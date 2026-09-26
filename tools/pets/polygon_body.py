"""Continuous quad skin for a Finik companion (Blender 5.2, Z up, front -Y).

``create_body`` makes one connected mesh for the torso, head, both arms and
both legs.  The surface has arm openings in the torso grid and two leg openings
under the pelvis; their boundaries continue into articulated limbs.  It does
not use Blender primitive operators, booleans, voxel remeshing, or loose shells.

The optional rig uses the bone names in ``build_pets.PetBuilder.make_rig``.
Weights are normalized before assignment.  Shoulder and hip seams mix the
body bone with the first limb bone; two loops on each side of the elbow/knee
mix adjacent limb bones.  This keeps the topology and weights useful for
animation in Blender and for glTF skinning in SceneView/Filament.
"""

from __future__ import annotations

import math

import bpy
from mathutils import Vector, geometry


SEGMENTS = 32

# A meridian profile gives the pear torso, narrow neck and large animal head.
# Extra levels around the shoulders and hips create deformation loops.
_PROFILE = (
    (.72, .44, .30),
    (.76, .45, .31), (.80, .46, .32), (.88, .45, .33),
    (.98, .42, .32), (1.08, .39, .30), (1.16, .37, .29),
    (1.24, .37, .28), (1.30, .38, .29), (1.34, .39, .29),
    (1.38, .40, .30), (1.42, .40, .30), (1.46, .39, .30),
    (1.50, .36, .28), (1.54, .32, .25), (1.58, .27, .22),
    (1.62, .23, .20), (1.68, .27, .23), (1.76, .39, .29),
    (1.84, .50, .36), (1.94, .57, .42), (2.05, .62, .46),
    (2.16, .64, .47), (2.28, .59, .44), (2.39, .51, .39),
    (2.48, .40, .32), (2.56, .29, .25), (2.62, .18, .16),
)

# Shoulder opening: angle start, number of angular faces, bottom and top Z.
# Its oriented rim becomes the first loop of an arm.
_OPENINGS = (
    ("Arm.L", 29, 6, 1.34, 1.50),
    ("Arm.R", 13, 6, 1.34, 1.50),
)


def _blend(a: dict[str, float], b: dict[str, float], t: float) -> dict[str, float]:
    return {key: a.get(key, 0) * (1 - t) + b.get(key, 0) * t
            for key in set(a) | set(b)}


def _body_weights(z: float) -> dict[str, float]:
    spans = (
        (.84, 1.18, "Hips", "Spine"),
        (1.18, 1.42, "Spine", "Chest"),
        (1.56, 1.76, "Chest", "Head"),
    )
    if z <= .84:
        return {"Hips": 1.0}
    if z < 1.18:
        a, b, one, two = spans[0]
    elif z < 1.42:
        a, b, one, two = spans[1]
    elif z < 1.56:
        return {"Chest": 1.0}
    elif z < 1.76:
        a, b, one, two = spans[2]
    else:
        return {"Head": 1.0}
    t = max(0.0, min(1.0, (z - a) / (b - a)))
    t = t * t * (3 - 2 * t)
    return {one: 1 - t, two: t}


def _shoulder_weights(x: float, y: float, z: float,
                      torso: dict[str, float]) -> dict[str, float]:
    """Spread upper-arm influence across torso loops around the arm socket.

    A shared polygon rim alone would crease sharply when the hand reaches the
    mouth.  Nearby shoulder quads therefore blend gradually into the arm bone.
    """
    for side, sign in (("L", 1), ("R", -1)):
        x_falloff = max(0.0, 1.0 - abs(x-sign*.38)/.38)
        # Keep a level weight field over the socket itself.  A triangular
        # falloff made adjacent torso rows at 1.38/1.42 collapse in Wave.
        z_falloff = max(0.0, min(1.0, (z-1.18)/.16, (1.66-z)/.16))
        y_falloff = max(0.0, 1.0 - abs(y)/.55)
        influence = .56 * x_falloff * z_falloff * (.65+.35*y_falloff)
        if influence > 1e-5:
            torso = _blend(torso, {f"UpperArm.{side}": 1}, influence)
    return torso


def _dims(species: str, z: float, rx: float, ry: float) -> tuple[float, float]:
    if species == "owl":
        return rx * (1.05 if z >= 1.68 else 1.04), ry * (.94 if z >= 1.68 else 1.10)
    if species == "dog":
        return rx * (.96 if z >= 1.68 else 1.03), ry * (.97 if z >= 1.68 else 1.02)
    return rx, ry


def create_body(
    species: str,
    *,
    rig: bpy.types.Object | None = None,
    primary_material: bpy.types.Material | None = None,
    secondary_material: bpy.types.Material | None = None,
    collection: bpy.types.Collection | None = None,
    name: str = "PolygonBody",
    add_armature_modifier: bool = True,
) -> bpy.types.Object:
    """Create a single, closed skinned mesh containing head, torso and limbs.

    ``species`` is ``cat``, ``owl`` or ``dog``.  The returned object is at the
    world origin, feet at Z=0.035, head to Z=2.65, facing -Y.  Normalized
    vertex groups are added when ``rig`` is supplied, along with an armature
    modifier unless ``add_armature_modifier`` is false.
    Pass ``add_armature_modifier=False`` when joining this object into a mesh
    that already receives an armature modifier (e.g. ``PetBuilder.join_skin``).
    Extra ears, muzzle, tail, face and accessories may be attached separately
    to their corresponding bones; they do not interrupt the main body skin.
    """
    if species not in {"cat", "owl", "dog"}:
        raise ValueError(f"Unknown companion species: {species}")

    verts: list[tuple[float, float, float]] = []
    faces: list[tuple[int, ...]] = []
    secondary_faces: set[int] = set()
    weights: list[dict[str, float]] = []

    def vertex(co: Vector | tuple[float, float, float], skin: dict[str, float]) -> int:
        i = len(verts)
        verts.append(tuple(co))
        positive = {bone: weight for bone, weight in skin.items() if weight > 1e-7}
        total = sum(positive.values())
        if total <= 0:
            raise ValueError("Unweighted body vertex")
        weights.append({bone: weight / total for bone, weight in positive.items()})
        return i

    level = {z: i for i, (z, _, _) in enumerate(_PROFILE)}
    holes = {}
    for label, start, width, lo, hi in _OPENINGS:
        holes[label] = (start, width, level[lo], level[hi])

    # Body rows share vertices, so the head/neck/torso is one continuous skin.
    rows: list[list[int]] = []
    for z, base_rx, base_ry in _PROFILE:
        rx, ry = _dims(species, z, base_rx, base_ry)
        centre_y = -.015 if z >= 1.76 else 0.0
        row = []
        for j in range(SEGMENTS):
            theta = 2 * math.pi * j / SEGMENTS
            x = rx * math.cos(theta)
            y = centre_y + ry * math.sin(theta)
            row.append(vertex((x, y, z),
                              _shoulder_weights(x, y, z, _body_weights(z))))
        rows.append(row)

    # The top cap contains triangles; the lower torso becomes a pair-of-pants
    # surface with two openings for the continuous legs.
    top = vertex((0, -.015, 2.65), {"Head": 1})
    for j in range(SEGMENTS):
        nxt = (j + 1) % SEGMENTS
        faces.append((rows[-1][j], rows[-1][nxt], top))

    def removed(row_index: int, angular_face: int) -> bool:
        if (level[.88] <= row_index < level[1.42]
                and 19 <= angular_face < 29):
            return True
        for start, width, lo, hi in holes.values():
            if lo <= row_index < hi and (angular_face - start) % SEGMENTS < width:
                return True
        return False

    for k in range(len(rows) - 1):
        for j in range(SEGMENTS):
            if not removed(k, j):
                nxt = (j + 1) % SEGMENTS
                faces.append((rows[k][j], rows[k][nxt],
                              rows[k + 1][nxt], rows[k + 1][j]))

    # Perimeter follows the orientation of the patch that was removed.  New
    # tube quads therefore oppose the surrounding body edges exactly once.
    def opening_rim(start: int, width: int, lo: int, hi: int) -> list[int]:
        return (
            [rows[lo][(start + step) % SEGMENTS] for step in range(width + 1)]
            + [rows[k][(start + width) % SEGMENTS] for k in range(lo + 1, hi + 1)]
            + [rows[hi][(start + step) % SEGMENTS] for step in range(width - 1, -1, -1)]
            + [rows[k][start] for k in range(hi - 1, lo, -1)]
        )

    def tube(label: str, rings: list[tuple[tuple[float, float, float], float, float,
                                           dict[str, float]]], tip: tuple[float, float, float],
             tip_skin: dict[str, float]) -> None:
        start, width, lo, hi = holes[label]
        rim = opening_rim(start, width, lo, hi)
        boundary = [Vector(verts[i]) for i in rim]
        centre = sum(boundary, Vector()) / len(boundary)
        side = 1.0 if label.endswith(".L") else -1.0
        origin_axis = Vector((side, 0, 0))
        rel = [point - centre for point in boundary]
        y_extent = max(abs(v.y) for v in rel)
        z_extent = max(abs(v.z) for v in rel)
        radial = []
        for v in rel:
            unit = Vector((0, v.y / y_extent, v.z / z_extent)).normalized()
            radial.append(unit)

        base_bone = "UpperArm" + label[-2:]
        # The body vertices surrounding the opening deform with the shoulder.
        # They are shared with the body surface, not duplicated.
        influence = .12
        for i in rim:
            weights[i] = _blend(weights[i], {base_bone: 1}, influence)

        prior = rim
        centres = [centre] + [Vector(spec[0]) for spec in rings] + [Vector(tip)]
        for index, (co, ry, rz, skin) in enumerate(rings, start=1):
            # Rotate the opening plane continuously with the arm tangent.
            # The loop's winding is preserved as the arm bends downward.
            tangent = (centres[index + 1] - centres[index - 1]).normalized()
            rotation = origin_axis.rotation_difference(tangent)
            co = Vector(co)
            current = []
            for direction in radial:
                local = Vector((0, direction.y * ry, direction.z * rz))
                current.append(vertex(co + rotation @ local, skin))
            for n in range(len(rim)):
                following = (n + 1) % len(rim)
                faces.append((prior[n], prior[following], current[following], current[n]))
                if index >= 10 and species != "owl":
                    secondary_faces.add(len(faces) - 1)
            prior = current
        end = vertex(tip, tip_skin)
        for n in range(len(prior)):
            faces.append((prior[n], prior[(n + 1) % len(prior)], end))
            if species != "owl":
                secondary_faces.add(len(faces) - 1)

    for side_name, sign in (("L", 1), ("R", -1)):
        upper = f"UpperArm.{side_name}"
        fore = f"Forearm.{side_name}"
        hand = f"Hand.{side_name}"
        arm_rings = [
            ((sign * .50, -.005, 1.40), .115, .115, {upper: 1}),
            ((sign * .56, -.010, 1.35), .135, .130, {upper: 1}),
            ((sign * .62, -.015, 1.27), .150, .145, {upper: 1}),
            ((sign * .66, -.025, 1.20), .136, .132, {upper: 1}),
            ((sign * .67, -.035, 1.14), .130, .126, _blend({upper: 1}, {fore: 1}, .4)),
            ((sign * .68, -.045, 1.09), .127, .123, _blend({upper: 1}, {fore: 1}, .75)),
            ((sign * .69, -.060, .99), .120, .117, {fore: 1}),
            ((sign * .70, -.075, .89), .123, .121, _blend({fore: 1}, {hand: 1}, .45)),
            ((sign * .70, -.092, .82), .157, .145, {hand: 1}),
            ((sign * .70, -.110, .75), .128, .122, {hand: 1}),
        ]
        if species == "owl":
            arm_rings = [(co, ry * 1.18, rz * .90, skin) for co, ry, rz, skin in arm_rings]
        tube(f"Arm.{side_name}", arm_rings,
             (sign * .70, -.12, .70), {hand: 1})

    # Pair-of-pants topology: tessellate the underside of the pelvis between
    # its one outer ring and two leg openings.  This keeps the thighs below the
    # hips and close to their rig bones, rather than branching from the sides.
    leg_openings: dict[str, list[int]] = {}
    LEG_SEGMENTS = 20
    for side_name, sign in (("L", 1), ("R", -1)):
        thigh = f"Thigh.{side_name}"
        shin = f"Shin.{side_name}"
        foot = f"Foot.{side_name}"
        first = []
        for j in range(LEG_SEGMENTS):
            angle = -2 * math.pi * j / LEG_SEGMENTS  # -Z-facing opening
            first.append(vertex((sign * .23 + .16 * math.cos(angle),
                                 .16 * math.sin(angle), .66),
                                _blend({"Hips": 1}, {thigh: 1}, .4)))
        leg_openings[side_name] = first

        leg_rings = [
            ((sign * .23, 0, .62), .17, .16, _blend({"Hips": 1}, {thigh: 1}, .7)),
            ((sign * .24, 0, .56), .175, .165, {thigh: 1}),
            ((sign * .25, 0, .48), .165, .16, {thigh: 1}),
            ((sign * .25, 0, .40), .155, .155, {thigh: 1}),
            ((sign * .25, 0, .36), .15, .15, _blend({thigh: 1}, {shin: 1}, .3)),
            ((sign * .25, 0, .32), .145, .145, _blend({thigh: 1}, {shin: 1}, .65)),
            ((sign * .25, 0, .28), .145, .145, {shin: 1}),
            ((sign * .25, -.01, .22), .145, .15, {shin: 1}),
            ((sign * .25, -.03, .18), .15, .16, _blend({shin: 1}, {foot: 1}, .5)),
            ((sign * .25, -.07, .14), .17, .18, {foot: 1}),
            ((sign * .25, -.12, .10), .19, .21, {foot: 1}),
            ((sign * .25, -.14, .06), .17, .18, {foot: 1}),
        ]
        prior = first
        for index, ((cx, cy, z), rx, ry, skin) in enumerate(leg_rings, start=1):
            current = []
            for j in range(LEG_SEGMENTS):
                angle = -2 * math.pi * j / LEG_SEGMENTS
                current.append(vertex((cx + rx * math.cos(angle),
                                       cy + ry * math.sin(angle), z), skin))
            for j in range(LEG_SEGMENTS):
                nxt = (j + 1) % LEG_SEGMENTS
                faces.append((prior[j], prior[nxt], current[nxt], current[j]))
                if index >= 11:
                    secondary_faces.add(len(faces) - 1)
            prior = current
        tip = vertex((sign * .25, -.14, .035), {foot: 1})
        for j in range(LEG_SEGMENTS):
            faces.append((prior[j], prior[(j + 1) % LEG_SEGMENTS], tip))
            secondary_faces.add(len(faces) - 1)

    contours = [rows[0], leg_openings["L"], leg_openings["R"]]
    flat = [i for contour in contours for i in contour]
    projected = [[Vector((verts[i][0], verts[i][1], 0)) for i in contour]
                 for contour in contours]
    for a, b, c in geometry.tessellate_polygon(projected):
        # Tessellation faces +Z; the exposed pelvis underside faces -Z.
        faces.append((flat[c], flat[b], flat[a]))

    # Build the pale belly as a contoured material island in the *same* skin.
    # Its boundary is sampled on the torso surface, so the material edge is a
    # smooth ellipse rather than a staircase of existing large quad faces.
    # The surrounding primary-fur patch and the pale centre share vertices.
    belly_rim = opening_rim(19, 10, level[.88], level[1.42])

    def surface(theta: float, z: float) -> tuple[float, float, float]:
        for (z0, rx0, ry0), (z1, rx1, ry1) in zip(_PROFILE, _PROFILE[1:]):
            if z0 <= z <= z1:
                blend = (z - z0) / (z1 - z0)
                rx0, ry0 = _dims(species, z0, rx0, ry0)
                rx1, ry1 = _dims(species, z1, rx1, ry1)
                rx = rx0 * (1 - blend) + rx1 * blend
                ry = ry0 * (1 - blend) + ry1 * blend
                return rx * math.cos(theta), ry * math.sin(theta), z
        raise ValueError(f"Belly vertex Z {z} outside torso profile")

    BELLY_STEPS = 64
    belly_outer: list[int] = []
    belly_inner: list[int] = []
    uv_outer: list[Vector] = []
    for j in range(BELLY_STEPS):
        angle = 2 * math.pi * j / BELLY_STEPS
        theta = 1.5 * math.pi + .90 * math.cos(angle)
        z = 1.15 + .245 * math.sin(angle)
        belly_outer.append(vertex(surface(theta, z),
                                  _shoulder_weights(*surface(theta, z), _body_weights(z))))
        uv_outer.append(Vector((theta, z, 0)))
        inner_theta = 1.5 * math.pi + .47 * math.cos(angle)
        inner_z = 1.15 + .125 * math.sin(angle)
        belly_inner.append(vertex(surface(inner_theta, inner_z),
                                  _shoulder_weights(*surface(inner_theta, inner_z),
                                                    _body_weights(inner_z))))

    def belly_uv(index: int) -> Vector:
        x, y, z = verts[index]
        theta = math.atan2(y, x)
        if theta < 0:
            theta += 2 * math.pi
        return Vector((theta, z, 0))

    outer_uv = [belly_uv(i) for i in belly_rim]
    # The inner loop must be clockwise for tessellation of a region with a hole.
    flat = belly_rim + list(reversed(belly_outer))
    for a, b, c in geometry.tessellate_polygon([outer_uv, list(reversed(uv_outer))]):
        faces.append((flat[a], flat[b], flat[c]))

    for j in range(BELLY_STEPS):
        nxt = (j + 1) % BELLY_STEPS
        faces.append((belly_outer[j], belly_outer[nxt],
                      belly_inner[nxt], belly_inner[j]))
        secondary_faces.add(len(faces) - 1)
    centre = vertex(surface(1.5 * math.pi, 1.15),
                    _body_weights(1.15))
    for j in range(BELLY_STEPS):
        faces.append((belly_inner[j], belly_inner[(j + 1) % BELLY_STEPS], centre))
        secondary_faces.add(len(faces) - 1)

    # Interior grid vertices of each removed patch have no faces.  Strip them
    # before creating the Blender mesh so the exported skin is one component.
    used = sorted({i for face in faces for i in face})
    remap = {old: new for new, old in enumerate(used)}
    verts = [verts[i] for i in used]
    weights = [weights[i] for i in used]
    faces = [tuple(remap[i] for i in face) for face in faces]

    mesh = bpy.data.meshes.new(name + "Mesh")
    mesh.from_pydata(verts, [], faces)
    mesh.update(calc_edges=True)
    obj = bpy.data.objects.new(name, mesh)
    (collection or bpy.context.collection).objects.link(obj)
    if primary_material is not None:
        mesh.materials.append(primary_material)
    if secondary_material is not None:
        mesh.materials.append(secondary_material)
    for index, polygon in enumerate(mesh.polygons):
        polygon.use_smooth = True
        if secondary_material is not None and index in secondary_faces:
            polygon.material_index = 1
    obj["topology"] = "single connected polygon skin; quad body and limb loops"
    obj["species"] = species
    obj["front"] = "-Y"

    if rig is not None:
        if rig.type != "ARMATURE":
            raise TypeError("rig must be a Blender armature object")
        needed = sorted({bone for item in weights for bone in item})
        missing = set(needed) - {bone.name for bone in rig.data.bones}
        if missing:
            raise ValueError(f"Armature lacks required bones: {sorted(missing)}")
        groups = {bone: obj.vertex_groups.new(name=bone) for bone in needed}
        for i, skin in enumerate(weights):
            for bone, amount in skin.items():
                if amount > 1e-7:
                    groups[bone].add([i], amount, "REPLACE")
        if add_armature_modifier:
            modifier = obj.modifiers.new("Finik weighted skeleton", "ARMATURE")
            modifier.object = rig

    return obj
