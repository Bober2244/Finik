"""Purpose-built polygon details for the three Finik companions.

All coordinates are Blender Z-up, with the face toward -Y.  The shapes are
authored from vertex loops, contours and feather strips.  No Blender primitive
mesh operators, curves, bevel modifiers or remeshing are used here.

``build_details`` works with ``PetBuilder.assign`` from build_pets.py, which
collects the pieces for the final combined glTF skin.  It can also create its
own armature modifiers for standalone Blender inspection.
"""

from __future__ import annotations

import math
from collections.abc import Callable, Mapping

import bpy
from mathutils import Vector


def _mesh(name, vertices, faces, material, bone, ctx):
    mesh = bpy.data.meshes.new(name)
    mesh.from_pydata(vertices, [], faces)
    mesh.update(calc_edges=True)
    obj = bpy.data.objects.new(name, mesh)
    ctx["collection"].objects.link(obj)
    assign = ctx["assign"]
    if assign is not None:
        assign(obj, material, bone, "PetDetails")
    else:
        obj.data.materials.append(ctx["materials"][material])
        group = obj.vertex_groups.new(name=bone)
        group.add(list(range(len(vertices))), 1.0, "REPLACE")
        for polygon in mesh.polygons:
            polygon.use_smooth = True
        if ctx["rig"] is not None:
            modifier = obj.modifiers.new("Finik weighted skeleton", "ARMATURE")
            modifier.object = ctx["rig"]
    ctx["objects"].append(obj)
    return obj


def _faceted_patch(name, outline, back_y, front_y, material, bone, ctx,
                   *, crown=.76, center_drop=.012):
    """Closed convex face patch with a shaped border and raised inner loop.

    ``outline`` is an anticlockwise X/Z contour viewed from the front.  The
    two authored contour loops create an explicit bevel, while the crown
    preserves the recognisable almond/heart/stripe outlines of each part.
    """
    n = len(outline)
    cx = sum(point[0] for point in outline) / n
    cz = sum(point[1] for point in outline) / n
    verts = [(x, back_y, z) for x, z in outline]
    verts += [(cx + crown * (x - cx), front_y, cz + crown * (z - cz))
              for x, z in outline]
    verts.append((cx, front_y - center_drop, cz))
    faces = []
    for j in range(n):
        nxt = (j + 1) % n
        faces.append((j, nxt, n + nxt, n + j))
        faces.append((2*n, n + j, n + nxt))
    faces.append(tuple(reversed(range(n))))
    return _mesh(name, verts, faces, material, bone, ctx)


def _contour(cx, cz, rx, rz, *, n=14, shape="round"):
    out = []
    for i in range(n):
        angle = 2 * math.pi * i / n
        c, s = math.cos(angle), math.sin(angle)
        if shape == "cat_eye":
            # Taper both outer corners, keep a high arched upper lid.
            width = 1.0 + .08 * s
            x = cx + rx * c * width
            z = cz + rz * math.copysign(abs(s)**1.35, s) * (1.06 if s > 0 else .82)
        elif shape == "dog_eye":
            x = cx + rx * c * (1.02 + .08 * s)
            z = cz + rz * s * (.98 if s > 0 else .86)
        elif shape == "owl_disk":
            x = cx + rx * c * (1.0 + .05 * s)
            z = cz + rz * s * (1.0 - .13 * max(0, -s))
        elif shape == "muzzle":
            x = cx + rx * c * (1.02 + .08 * max(0, -s))
            z = cz + rz * s * (.82 if s > 0 else 1.03)
        else:
            x = cx + rx * c
            z = cz + rz * s
        out.append((x, z))
    return out


def _head_front_y(x, z, species):
    """Sample the forehead of polygon_body's head profile for flush markings."""
    levels = ((1.76, .39, .29), (1.84, .50, .36),
              (1.94, .57, .42), (2.05, .62, .46),
              (2.16, .64, .47), (2.28, .59, .44),
              (2.39, .51, .39), (2.48, .40, .32),
              (2.56, .29, .25), (2.62, .18, .16))
    if z <= levels[0][0]:
        radius_x, radius_y = levels[0][1:]
    elif z >= levels[-1][0]:
        radius_x, radius_y = levels[-1][1:]
    else:
        for lower, upper in zip(levels, levels[1:]):
            if lower[0] <= z <= upper[0]:
                mix = (z - lower[0]) / (upper[0] - lower[0])
                radius_x = lower[1]*(1-mix) + upper[1]*mix
                radius_y = lower[2]*(1-mix) + upper[2]*mix
                break
    if species == "dog":
        radius_x *= .96
        radius_y *= .97
    elif species == "owl":
        radius_x *= 1.05
        radius_y *= .94
    relative_x = min(abs(x) / radius_x, .985)
    return -.015 - radius_y*math.sqrt(1-relative_x*relative_x)


def _surface_mark(name, outline, species, material, bone, ctx,
                  *, offset=.017):
    """A closed thin polygonal patch draped over the head's quad surface."""
    area = sum(x0*z1 - x1*z0 for (x0,z0),(x1,z1)
               in zip(outline, outline[1:] + outline[:1]))
    if area < 0:
        outline = list(reversed(outline))
    cx = sum(x for x, _ in outline) / len(outline)
    cz = sum(z for _, z in outline) / len(outline)
    n = len(outline)
    verts = [(x, _head_front_y(x, z, species)-offset, z)
             for x, z in outline]
    verts.append((cx, _head_front_y(cx, cz, species)-offset-.004, cz))
    verts.extend((x, _head_front_y(x, z, species)-offset+.006, z)
                 for x, z in outline)
    verts.append((cx, _head_front_y(cx, cz, species)-offset+.006, cz))
    front_center, back_start, back_center = n, n+1, 2*n+1
    faces = []
    for j in range(n):
        nxt = (j+1) % n
        faces.append((front_center, j, nxt))
        faces.append((nxt, j, back_start+j, back_start+nxt))
        faces.append((back_center, back_start+nxt, back_start+j))
    return _mesh(name, verts, faces, material, bone, ctx)


def _strip(name, centers, half_widths, front_y, back_y, material, bone, ctx):
    """A tapered feather/marking ribbon with a raised lengthwise ridge."""
    if len(centers) != len(half_widths):
        raise ValueError("Strip centers and widths must match")
    verts = []
    for i, (x, z) in enumerate(centers):
        prev = Vector(centers[max(0, i-1)])
        nxt = Vector(centers[min(len(centers)-1, i+1)])
        tangent = (nxt - prev).normalized()
        normal = Vector((tangent.y, -tangent.x))
        width = half_widths[i]
        verts.extend([
            (x - normal.x*width, back_y, z - normal.y*width),
            (x - normal.x*width*.83, front_y, z - normal.y*width*.83),
            (x, front_y-.024, z),
            (x + normal.x*width*.83, front_y, z + normal.y*width*.83),
            (x + normal.x*width, back_y, z + normal.y*width),
        ])
    faces = []
    for j in range(len(centers)-1):
        for k in range(4):
            faces.append((5*j+k, 5*(j+1)+k, 5*(j+1)+k+1, 5*j+k+1))
        faces.append((5*j, 5*j+4, 5*(j+1)+4, 5*(j+1)))
    faces.extend([(0, 1, 2, 3, 4),
                  tuple(5*(len(centers)-1)+k for k in range(4, -1, -1))])
    return _mesh(name, verts, faces, material, bone, ctx)


def _loft(name, rings, material, bone, ctx, segments=10):
    """Closed tapered 3-D strip/tube with independently placed polygon loops.

    A ring is ``(center_xyz, width_x, depth_y)``.  Its plane is horizontal;
    this suits upright ears and the predominantly vertical curved tails.
    """
    vertices = []
    for center, width, depth in rings:
        x, y, z = center
        for j in range(segments):
            theta = 2*math.pi*j/segments
            vertices.append((x+width*math.cos(theta),
                             y+depth*math.sin(theta), z))
    faces = []
    for k in range(len(rings)-1):
        for j in range(segments):
            nxt = (j+1) % segments
            faces.append((k*segments+j, k*segments+nxt,
                          (k+1)*segments+nxt, (k+1)*segments+j))
    faces.append(tuple(reversed(range(segments))))
    faces.append(tuple((len(rings)-1)*segments+j for j in range(segments)))
    return _mesh(name, vertices, faces, material, bone, ctx)


def _polyline(name, points, radius, material, bone, ctx):
    """Four-sided swept polygon wire, used for mouth lines and whiskers."""
    verts = []
    n = len(points)
    for i, point in enumerate(points):
        tangent = (Vector(points[min(n-1, i+1)]) -
                   Vector(points[max(0, i-1)])).normalized()
        in_face = Vector((tangent.z, 0, -tangent.x)).normalized()
        toward_view = Vector((0, -1, 0))
        for a, b in ((1, 1), (-1, 1), (-1, -1), (1, -1)):
            verts.append(tuple(Vector(point) + radius*(a*in_face+b*toward_view)))
    faces = []
    for i in range(n-1):
        for j in range(4):
            faces.append((i*4+j, i*4+(j+1)%4,
                          (i+1)*4+(j+1)%4, (i+1)*4+j))
    faces.append((3, 2, 1, 0))
    faces.append(tuple((n-1)*4+j for j in range(4)))
    return _mesh(name, verts, faces, material, bone, ctx)


def _triangular_nose(name, center_x, center_z, width, height,
                     back_y, front_y, material, bone, ctx):
    x, z = center_x, center_z
    outline = [(x+width*.46, z+height*.34),
               (x+width*.29, z+height*.50),
               (x-width*.29, z+height*.50),
               (x-width*.46, z+height*.34),
               (x-width*.15, z-height*.45),
               (x, z-height*.57),
               (x+width*.15, z-height*.45)]
    # Reverse into anticlockwise X/Z order for front-facing normals.
    return _faceted_patch(name, list(reversed(outline)), back_y, front_y,
                          material, bone, ctx, crown=.72)


def _eyes(species, ctx):
    for side, sign in (("L", 1), ("R", -1)):
        x = sign*(.247 if species == "owl" else .255)
        z = 2.13
        if species == "owl":
            # The pale disks follow the curved quad head.  The nested iris
            # and pupil layers remain colourable without a protruding goggle.
            _surface_mark(f"Owl facial disk {side}",
                          _contour(x, z, .306, .326, n=18,
                                   shape="owl_disk"),
                          species, "FurSecondary", "Head", ctx,
                          offset=.018)
            _surface_mark(f"Owl eye rim {side}",
                          _contour(x, z, .172, .193, n=16),
                          species, "FurPrimary", "Head", ctx,
                          offset=.027)
            _surface_mark(f"Iris {side}",
                          _contour(x, z, .149, .172, n=16),
                          species, "EyeIris", "Head", ctx,
                          offset=.038)
            _surface_mark(f"Pupil {side}",
                          _contour(x, z, .075, .105, n=12),
                          species, "FaceDark", "Head", ctx,
                          offset=.050)
            glint_x, glint_z = x-sign*.035, z+.056
        else:
            cat = species == "cat"
            shape = "cat_eye" if cat else "dog_eye"
            ew, eh = (.181, .212) if cat else (.170, .192)
            iris_w, iris_h = (.099, .136) if cat else (.096, .121)
            pupil_w, pupil_h = (.038, .104) if cat else (.048, .088)
            _surface_mark(f"Eye outline {side}",
                          _contour(x, z, ew*1.055, eh*1.055,
                                   n=16, shape=shape),
                          species, "FaceDark", "Head", ctx,
                          offset=.023)
            _surface_mark(f"Eye white {side}",
                          _contour(x, z, ew, eh, n=16, shape=shape),
                          species, "EyeWhite", "Head", ctx,
                          offset=.034)
            iris_x = x-sign*.013
            _surface_mark(f"Iris {side}",
                          _contour(iris_x, z-.004, iris_w, iris_h,
                                   n=14),
                          species, "EyeIris", "Head", ctx,
                          offset=.045)
            _surface_mark(f"Pupil {side}",
                          _contour(iris_x, z-.004, pupil_w, pupil_h,
                                   n=12),
                          species, "FaceDark", "Head", ctx,
                          offset=.056)
            glint_x, glint_z = iris_x-sign*.030, z+.052
        _surface_mark(f"Eye glint {side}",
                      _contour(glint_x, glint_z, .024, .030, n=8),
                      species, "EyeWhite", "Head", ctx,
                      offset=.065)
        brow_points = [(x-sign*.126, 2.410),
                       (x, 2.444),
                       (x+sign*.122, 2.414)]
        _polyline(f"Brow {side}",
                  [(bx, _head_front_y(bx, bz, species)-.040, bz)
                   for bx, bz in brow_points],
                  .012 if species == "owl" else .013,
                  "FurPrimary" if species == "owl" else "FaceDark",
                  f"Brow.{side}", ctx)


def _cat(ctx):
    for side, sign in (("L", 1), ("R", -1)):
        rings = [
            ((sign*.43, -.045, 2.43), .170, .170),
            ((sign*.46, -.050, 2.53), .177, .154),
            ((sign*.49, -.041, 2.66), .120, .126),
            ((sign*.515, -.028, 2.79), .057, .085),
            ((sign*.525, -.015, 2.88), .008, .014),
        ]
        _loft(f"Cat pointed ear {side}", rings, "FurPrimary", f"Ear.{side}", ctx)
        _strip(f"Cat ear lining {side}",
               [(sign*.455, 2.51), (sign*.48, 2.64), (sign*.515, 2.78)],
               [.105, .069, .008], -.222, -.188,
               "NosePink", f"Ear.{side}", ctx)

    for sign in (-1, 1):
        side = "L" if sign > 0 else "R"
        _faceted_patch(f"Cat muzzle pad {side}",
                       _contour(sign*.112, 1.882, .170, .116,
                                shape="muzzle"),
                       -.446, -.588, "FurSecondary", "Jaw", ctx)
        for j in range(2):
            z = 1.925-j*.045
            _polyline(f"Whisker {side} {j}",
                      [(sign*.235, -.592, z),
                       (sign*.40, -.572, z+.016-j*.004),
                       (sign*.61, -.520, z+.045-j*.031)],
                      .0045, "FurSecondary", "Head", ctx)
        _faceted_patch(f"Whisker dots {side}",
                       _contour(sign*.204, 1.888, .014, .013, n=8),
                       -.594, -.598, "FaceDark", "Jaw", ctx)
    _triangular_nose("Cat heart nose", 0, 1.975, .180, .111,
                     -.575, -.635, "NosePink", "Head", ctx)
    _polyline("Cat mouth stem", [(0, -.600, 1.929), (0, -.609, 1.876)],
              .009, "FaceDark", "Jaw", ctx)
    for sign in (-1, 1):
        _polyline(f"Cat smile {sign}",
                  [(0, -.609, 1.876), (sign*.062, -.604, 1.844),
                   (sign*.128, -.586, 1.862)],
                  .008, "FaceDark", "Jaw", ctx)
    for x in (-.155, 0, .155):
        _surface_mark(f"Cat forehead stripe {x:+.2f}",
                      [(x-.018, 2.515), (x+.018, 2.515),
                       (x+.038, 2.453), (x+.006, 2.409),
                       (x-.031, 2.453)],
                      "cat", "FurSecondary", "Head", ctx)
    _loft("Cat curled tail", [
        ((0, .29, .72), .080, .080),
        ((.13, .51, .72), .105, .102),
        ((.33, .68, .82), .109, .103),
        ((.47, .75, 1.01), .095, .091),
        ((.51, .69, 1.21), .082, .081),
        ((.46, .56, 1.33), .051, .053),
        ((.41, .48, 1.35), .008, .009),
    ], "FurPrimary", "Tail", ctx)


def _dog(ctx):
    for side, sign in (("L", 1), ("R", -1)):
        # Wide root, drooping middle and tapered rounded tip make the ears
        # read as floppy even while their Ear bones animate.
        _loft(f"Dog floppy ear {side}", [
            ((sign*.55, -.010, 2.43), .119, .130),
            ((sign*.64, -.027, 2.35), .144, .133),
            ((sign*.74, -.054, 2.22), .165, .127),
            ((sign*.81, -.078, 2.08), .157, .118),
            ((sign*.83, -.093, 1.96), .132, .103),
            ((sign*.81, -.101, 1.86), .085, .079),
            ((sign*.79, -.104, 1.82), .008, .011),
        ], "FurPrimary", f"Ear.{side}", ctx)
        _strip(f"Dog ear lining {side}",
               [(sign*.65, 2.32), (sign*.77, 2.17),
                (sign*.83, 2.00), (sign*.81, 1.89)],
               [.044, .062, .053, .006], -.205, -.176,
               "FurSecondary", f"Ear.{side}", ctx)
    _surface_mark("Dog forehead blaze",
                  [(-.105, 2.42), (-.059, 2.50), (0, 2.52),
                   (.060, 2.50), (.106, 2.42), (.069, 2.26),
                   (0, 2.19), (-.069, 2.26)],
                  "dog", "FurSecondary", "Head", ctx)
    # A long snout projects in front of the jowls; two side pads give a
    # clear dog silhouette from oblique camera angles.
    _faceted_patch("Dog central snout",
                   _contour(0, 1.936, .235, .166, n=14,
                            shape="muzzle"),
                   -.433, -.658, "FurSecondary", "Jaw", ctx, crown=.85)
    for sign in (-1, 1):
        _faceted_patch(f"Dog jowl {sign}",
                       _contour(sign*.153, 1.859, .155, .106, n=12,
                                shape="muzzle"),
                       -.462, -.609, "FurSecondary", "Jaw", ctx)
    _triangular_nose("Dog broad nose", 0, 1.999, .224, .147,
                     -.651, -.719, "FaceDark", "Head", ctx)
    _polyline("Dog mouth stem", [(0, -.666, 1.920), (0, -.682, 1.831)],
              .010, "FaceDark", "Jaw", ctx)
    for sign in (-1, 1):
        _polyline(f"Dog smile {sign}",
                  [(0, -.682, 1.831), (sign*.098, -.651, 1.812),
                   (sign*.181, -.597, 1.853)],
                  .009, "FaceDark", "Jaw", ctx)
    _loft("Dog wagging tail", [
        ((0, .30, .72), .075, .075),
        ((.045, .46, .78), .103, .095),
        ((.10, .62, .87), .105, .095),
        ((.18, .74, .99), .090, .082),
        ((.23, .77, 1.09), .064, .062),
        ((.24, .75, 1.13), .008, .009),
    ], "FurPrimary", "Tail", ctx)


def _owl(ctx):
    for side, sign in (("L", 1), ("R", -1)):
        _loft(f"Owl feather tuft {side}", [
            ((sign*.43, .026, 2.42), .112, .108),
            ((sign*.48, .038, 2.50), .104, .093),
            ((sign*.55, .045, 2.62), .071, .075),
            ((sign*.62, .058, 2.76), .008, .012),
        ], "FurPrimary", f"Ear.{side}", ctx)
        _strip(f"Owl tuft marking {side}",
               [(sign*.455, 2.49), (sign*.54, 2.63), (sign*.60, 2.73)],
               [.057, .038, .004], -.097, -.077,
               "FurSecondary", f"Ear.{side}", ctx)
        # Pointed individual primaries follow the forearm rather than the
        # whole torso; the bones carry these feathers during play and wave.
        for j in range(3):
            root_x = sign*(.67 + j*.016)
            root_z = 1.27-j*.066
            _strip(f"Owl wing feather {side} {j}",
                   [(root_x, root_z),
                    (sign*(.79+j*.025), root_z-.15),
                    (sign*(.90+j*.020), root_z-.32),
                    (sign*(.92+j*.015), root_z-.37)],
                   [.048, .068, .037, .003],
                   -.163-j*.006, -.102-j*.006,
                   "FurPrimary" if j < 2 else "FurSecondary",
                   f"Forearm.{side}", ctx)
    # A ridged wedge projects from between the two facial disks.
    verts = [(-.101, -.486, 2.020), (.101, -.486, 2.020),
             (.077, -.567, 1.957), (-.077, -.567, 1.957),
             (0, -.654, 1.932), (0, -.574, 1.852)]
    faces = [(0, 1, 2, 4, 3), (3, 4, 5), (4, 2, 5),
             (0, 3, 5), (1, 5, 2), (0, 5, 1)]
    _mesh("Owl hooked beak", verts, faces, "WarmGold", "Jaw", ctx)
    for j in range(3):
        x = (j-1)*.105
        _loft(f"Owl tail feather {j}", [
            ((x*.35, .34, .70), .047, .078),
            ((x*.68, .46, .68), .070, .087),
            ((x, .60, .66), .072, .078),
            ((x*1.15, .70, .65), .040, .054),
            ((x*1.2, .74, .64), .004, .007),
        ], "FurSecondary", "Tail", ctx, segments=8)
    for sign in (-1, 1):
        for j, z in enumerate((1.32, 1.19, 1.06)):
            _strip(f"Owl breast feather {sign} {j}",
                   [(sign*.05, z+.063), (sign*.11, z),
                    (sign*.075, z-.07)],
                   [.020, .055, .003], -.327, -.284,
                   "FurSecondary", "Chest", ctx)


def build_details(
    species: str,
    *,
    materials: Mapping[str, bpy.types.Material],
    rig: bpy.types.Object | None = None,
    collection: bpy.types.Collection | None = None,
    assign: Callable | None = None,
) -> list[bpy.types.Object]:
    """Build face, ears, tail and species details as polygon meshes.

    Pass ``assign=PetBuilder.assign`` when using the existing combined skin
    export: every object is collected into ``PetDetails`` and receives a bone
    vertex group.  Alternatively pass ``rig`` without ``assign`` to inspect
    the individually skinned objects in Blender.
    """
    if species not in {"cat", "owl", "dog"}:
        raise ValueError(f"Unknown companion species: {species}")
    required = {"FurPrimary", "FurSecondary", "EyeWhite", "EyeIris",
                "FaceDark", "NosePink", "WarmGold"}
    missing = required - materials.keys()
    if missing:
        raise ValueError(f"Missing Finik materials: {', '.join(sorted(missing))}")
    ctx = {"materials": materials, "rig": rig,
           "collection": collection or bpy.context.collection,
           "assign": assign, "objects": []}
    _eyes(species, ctx)
    {"cat": _cat, "owl": _owl, "dog": _dog}[species](ctx)
    return ctx["objects"]
