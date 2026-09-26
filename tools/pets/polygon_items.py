"""Purpose-shaped polygon meshes for the companion's wearable and held items.

This module intentionally does not call Blender's mesh primitive operators.  Every
surface is authored as rings, ribbons, or an extruded silhouette so the editable
.blend files retain useful edges rather than a collection of stock spheres.
"""

import math

import bpy
from mathutils import Vector


def _mesh(name, vertices, faces, material, bone, group, assign):
    data = bpy.data.meshes.new(name)
    data.from_pydata(vertices, [], faces)
    data.update()
    obj = bpy.data.objects.new(name, data)
    bpy.context.collection.objects.link(obj)
    return assign(obj, material, bone, group)


def _lathe(name, center, profile, material, bone, group, assign, sides=16):
    """Revolve a designed radius/z profile into a closed, mostly quad mesh."""
    cx, cy = center
    vertices = []
    for z, radius in profile:
        for i in range(sides):
            a = i * 2 * math.pi / sides
            vertices.append((cx + radius * math.cos(a), cy + radius * math.sin(a), z))
    faces = []
    for j in range(len(profile) - 1):
        for i in range(sides):
            n = (i + 1) % sides
            faces.append((j*sides+i, j*sides+n, (j+1)*sides+n, (j+1)*sides+i))
    faces.append(tuple(reversed(range(sides))))
    faces.append(tuple((len(profile)-1)*sides+i for i in range(sides)))
    return _mesh(name, vertices, faces, material, bone, group, assign)


def _extruded_outline(name, points, front_y, thickness, material, bone, group, assign):
    """Closed polygon outline with hand-authored silhouette and visible thickness."""
    n = len(points)
    vertices = [(x, front_y, z) for x, z in points]
    vertices += [(x, front_y+thickness, z) for x, z in points]
    faces = [tuple(reversed(range(n))), tuple(n+i for i in range(n))]
    faces += [(i, (i+1)%n, n+(i+1)%n, n+i) for i in range(n)]
    return _mesh(name, vertices, faces, material, bone, group, assign)


def _elliptic_band(name, radii, bottom, top, material, bone, group, assign, sides=24, center=(0,0)):
    """Curved fabric collar with outer and inner edge loops."""
    rx, ry = radii
    cx, cy = center
    vertices = []
    for z, scale in [(bottom, 1.), (top, 1.04), (top, .80), (bottom, .80)]:
        for i in range(sides):
            a = 2*math.pi*i/sides
            vertices.append((cx+rx*scale*math.cos(a), cy+ry*scale*math.sin(a), z+.012*math.sin(3*a)))
    faces = []
    for ring in range(4):
        next_ring = (ring + 1) % 4
        for i in range(sides):
            j = (i+1) % sides
            faces.append((ring*sides+i, ring*sides+j, next_ring*sides+j, next_ring*sides+i))
    return _mesh(name, vertices, faces, material, bone, group, assign)


def _leaf(name, center, width, height, material, bone, group, assign):
    cx, cy, cz = center
    return _extruded_outline(name, [
        (cx-.38*width, cz-.15*height), (cx, cz-.47*height),
        (cx+.5*width, cz), (cx, cz+.48*height),
        (cx-.32*width, cz+.12*height),
    ], cy, .013, material, bone, group, assign)


def build_items(assign):
    """Create all three skinned accessories and three skinned action props.

    ``assign`` is PetBuilder.assign and supplies material lookup and Blender
    vertex groups.  Named groups are later joined, each preserving a separate
    glTF skinned mesh node for runtime accessory toggling.
    """
    scarf = 'Accessory_Scarf'
    _elliptic_band('Scarf woven collar', (.295, .275), 1.50, 1.60,
                   'Accessory', 'Chest', scarf, assign)
    _extruded_outline('Scarf front knot', [
        (.075,1.51), (.10,1.59), (.18,1.63), (.245,1.57),
        (.22,1.49), (.145,1.47),
    ], -.315, .065, 'Accessory', 'Chest', scarf, assign)
    _extruded_outline('Scarf long fabric', [
        (.17,1.51), (.24,1.50), (.29,1.38), (.27,1.18),
        (.23,1.13), (.18,1.25), (.16,1.39),
    ], -.335, .025, 'Accessory', 'Chest', scarf, assign)
    _extruded_outline('Scarf short fabric', [
        (.13,1.52), (.17,1.49), (.12,1.36), (.02,1.31),
        (-.015,1.34), (.03,1.42),
    ], -.330, .025, 'Accessory', 'Chest', scarf, assign)
    _leaf('Scarf gold pin', (.185,-.358,1.55), .055, .055,
          'WarmGold', 'Chest', scarf, assign)

    bow = 'Accessory_Bow'
    for side in (-1, 1):
        _extruded_outline(f'Bow folded wing {side}', [
            (side*.03,1.55), (side*.08,1.63), (side*.24,1.68),
            (side*.29,1.63), (side*.26,1.49), (side*.10,1.48),
        ], -.315, .055, 'Accessory', 'Chest', bow, assign)
        _extruded_outline(f'Bow crease {side}', [
            (side*.105,1.55), (side*.20,1.64), (side*.155,1.535),
        ], -.374, .004, 'FurSecondary', 'Chest', bow, assign)
    _lathe('Bow centre', (0,-.355), [(1.49,.025),(1.51,.052),(1.57,.055),(1.60,.025)],
           'WarmGold', 'Chest', bow, assign, sides=12)

    cap = 'Accessory_Cap'
    # An asymmetrical beret crown, with enough rings to keep its brim soft.
    vertices = []
    rings = [(2.52,.25,.17),(2.55,.40,.32),(2.63,.38,.31),(2.69,.24,.20),(2.71,.065,.07)]
    sides = 24
    for z, rx, ry in rings:
        for i in range(sides):
            a = 2*math.pi*i/sides
            vertices.append((.06+rx*math.cos(a), .01+ry*math.sin(a), z+.015*math.cos(a)))
    faces = []
    for r in range(len(rings)-1):
        for i in range(sides):
            j=(i+1)%sides
            faces.append((r*sides+i,r*sides+j,(r+1)*sides+j,(r+1)*sides+i))
    faces.append(tuple(reversed(range(sides))))
    faces.append(tuple((len(rings)-1)*sides+i for i in range(sides)))
    _mesh('Cap tailored crown',vertices,faces,'Accessory','Head',cap,assign)
    _extruded_outline('Cap brim', [
        (-.27,2.52),(.34,2.52),(.26,2.49),(.08,2.46),(-.18,2.48),
    ], -.315, .10, 'Accessory','Head',cap,assign)
    _lathe('Cap button', (.07,.015), [(2.695,.025),(2.715,.045),(2.735,.025)],
           'WarmGold','Head',cap,assign,sides=12)

    food = 'Prop_Food'
    _lathe('Food hand-carved fruit', (-.67,-.075), [
        (.865,.012),(.89,.075),(.97,.107),(1.045,.093),(1.09,.037),(1.095,.008),
    ], 'Food','PropFood',food,assign)
    _lathe('Fruit stem',(-.67,-.075),[(1.09,.008),(1.12,.012),(1.155,.008)],
           'FurPrimary','PropFood',food,assign,sides=8)
    _leaf('Fruit leaf',(-.616,-.083,1.13),.105,.035,
          'Accessory','PropFood',food,assign)

    bottle = 'Prop_Bottle'
    _lathe('Reusable water bottle',(-.67,-.075),[
        (.82,.055),(.84,.078),(.875,.085),(1.08,.085),(1.115,.063),
        (1.128,.040),(1.20,.040),(1.225,.038),(1.233,.025),
    ],'WaterBottle','PropBottle',bottle,assign)
    _elliptic_band('Bottle paper label',(.084,.084),.93,1.025,
                   'EyeWhite','PropBottle',bottle,assign,sides=16,center=(-.67,-.075))
    _leaf('Bottle water symbol',(-.67,-.163,.976),.045,.060,
          'WaterBottle','PropBottle',bottle,assign)
    _elliptic_band('Bottle rim',(.040,.040),1.221,1.237,
                   'EyeWhite','PropBottle',bottle,assign,sides=12,center=(-.67,-.075))

    toy = 'Prop_Toy'
    # A stitched star toy: purpose-built silhouette is visibly distinct from
    # the spherical eyes, paws and other anatomy.
    star = []
    for i in range(10):
        a = math.pi/2+i*math.pi/5
        r = .17 if i%2 == 0 else .079
        star.append((.67+r*math.cos(a),1.02+r*math.sin(a)))
    _extruded_outline('Five point plush toy',star,-.17,.18,
                      'WarmGold','PropToy',toy,assign)
    _extruded_outline('Toy stitched centre',[
        (.62,.995),(.67,1.035),(.72,.995),(.67,.965),
    ],-.175,.005,'Accessory','PropToy',toy,assign)
