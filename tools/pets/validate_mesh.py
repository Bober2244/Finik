"""Audit the continuous companion skin in Blender source files and exported GLBs.

Usage (from any directory)::

    Blender --background --factory-startup --python tools/pets/validate_mesh.py -- \
        /Users/bi_ba/Blender/Finik_models

The directory must contain cat/owl/dog.blend or cat/owl/dog.glb. When both
formats are present, both are inspected. A JSON report is printed on a line
starting with FINIK_MESH_REPORT, and the process exits nonzero on an error.
Cosmetic face, ear, and accessory shells may be separate: PolygonBody itself
must be a single closed connected skin with smoothly weighted articulations.
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

import bmesh
import bpy
from mathutils import Vector


SPECIES = ("cat", "owl", "dog")
CLIPS = ("Idle", "Eat", "Drink", "Happy", "Sad", "Play", "Wave")
MATERIALS = ("FurPrimary", "FurSecondary", "EyeIris", "Accessory")
ACCESSORIES = ("Accessory_Scarf", "Accessory_Bow", "Accessory_Cap")
JOINTS = tuple(
    (label, parent, child)
    for side in ("L", "R")
    for label, parent, child in (
        (f"Shoulder.{side}", "Chest", f"UpperArm.{side}"),
        (f"Elbow.{side}", f"UpperArm.{side}", f"Forearm.{side}"),
        (f"Knee.{side}", f"Thigh.{side}", f"Shin.{side}"),
    )
)


def issue(report: dict, category: str, message: str) -> None:
    report[category].append(message)


def open_asset(path: Path) -> None:
    bpy.ops.wm.read_factory_settings(use_empty=True)
    if path.suffix == ".blend":
        bpy.ops.wm.open_mainfile(filepath=str(path), load_ui=False)
    else:
        bpy.ops.import_scene.gltf(filepath=str(path))


def topology(mesh: bpy.types.Mesh, *, weld_export_seams: bool = False) -> dict:
    bm = bmesh.new()
    try:
        bm.from_mesh(mesh)
        # glTF stores material primitives separately and duplicates vertices at
        # their boundaries. Rejoin only coincident positions in this temporary
        # audit mesh so an export seam is not mistaken for a disconnected body.
        if weld_export_seams:
            bmesh.ops.remove_doubles(bm, verts=list(bm.verts), dist=1e-6)
        bm.verts.ensure_lookup_table()
        neighbors = [[] for _ in bm.verts]
        for edge in bm.edges:
            a, b = (v.index for v in edge.verts)
            neighbors[a].append(b)
            neighbors[b].append(a)
        unseen = set(range(len(bm.verts)))
        components = []
        while unseen:
            start = unseen.pop()
            pending = [start]
            count = 1
            while pending:
                for other in neighbors[pending.pop()]:
                    if other in unseen:
                        unseen.remove(other)
                        pending.append(other)
                        count += 1
            components.append(count)
        return {
            "vertices": len(bm.verts),
            "faces": len(bm.faces),
            "components": sorted(components, reverse=True),
            "boundaryEdges": sum(len(edge.link_faces) == 1 for edge in bm.edges),
            "nonManifoldEdges": sum(len(edge.link_faces) != 2 for edge in bm.edges),
            "looseVertices": sum(not vertex.link_edges for vertex in bm.verts),
            "zeroAreaFaces": sum(face.calc_area() <= 1e-10 for face in bm.faces),
            "signedVolume": round(bm.calc_volume(signed=True), 5),
        }
    finally:
        bm.free()


def vertex_weights(obj: bpy.types.Object, rig: bpy.types.Object, report: dict) -> tuple[list[dict], dict]:
    names = {group.index: group.name for group in obj.vertex_groups}
    bones = {bone.name for bone in rig.data.bones}
    all_weights = []
    unweighted = unnormalized = over_four = unknown_bone = 0
    mixed_count = 0
    for vertex in obj.data.vertices:
        weights = {names[g.group]: g.weight for g in vertex.groups if g.weight > 1e-6}
        all_weights.append(weights)
        total = sum(weights.values())
        unweighted += total <= 1e-5
        unnormalized += total > 1e-5 and abs(total - 1.0) > .015
        over_four += len(weights) > 4
        unknown_bone += any(name not in bones for name in weights)
        mixed_count += sum(weight >= .15 for weight in weights.values()) >= 2
    metrics = {
        "unweighted": unweighted,
        "unnormalized": unnormalized,
        "overFourInfluences": over_four,
        "unknownBone": unknown_bone,
        "mixedVertices": mixed_count,
    }
    for label, count in metrics.items():
        if label != "mixedVertices" and count:
            issue(report, "errors", f"PolygonBody: {count} vertices {label}")
    if mixed_count < 30:
        issue(report, "errors", f"PolygonBody: only {mixed_count} vertices have meaningful mixed weights")
    return all_weights, metrics


def assign_action(rig: bpy.types.Object, action: bpy.types.Action, frame: float) -> None:
    rig.animation_data_create()
    rig.animation_data.action = action
    if action.slots:
        matching = next((slot for slot in action.slots if slot.target_id_type == "OBJECT"), None)
        if matching is not None:
            rig.animation_data.action_slot = matching
    bpy.context.scene.frame_set(int(frame), subframe=frame % 1)
    bpy.context.view_layer.update()


def evaluated_positions(obj: bpy.types.Object) -> list[Vector]:
    depsgraph = bpy.context.evaluated_depsgraph_get()
    evaluated = obj.evaluated_get(depsgraph)
    mesh = evaluated.to_mesh()
    try:
        return [evaluated.matrix_world @ vertex.co for vertex in mesh.vertices]
    finally:
        evaluated.to_mesh_clear()


def joint_angle(rig: bpy.types.Object, parent: str, child: str) -> float:
    parent_pose = rig.pose.bones[parent]
    child_pose = rig.pose.bones[child]
    parent_rest = parent_pose.bone.matrix_local
    child_rest = child_pose.bone.matrix_local
    rest_relative = parent_rest.inverted_safe() @ child_rest
    pose_relative = parent_pose.matrix.inverted_safe() @ child_pose.matrix
    delta = rest_relative.inverted_safe() @ pose_relative
    return abs(delta.to_quaternion().angle)


def audit_deformation(obj: bpy.types.Object, rig: bpy.types.Object, weights: list[dict], report: dict) -> dict:
    actions = {action.name: action for action in bpy.data.actions}
    rig.data.pose_position = "REST"
    bpy.context.view_layer.update()
    rest = evaluated_positions(obj)
    rig.data.pose_position = "POSE"
    if len(rest) != len(obj.data.vertices):
        issue(report, "errors", "PolygonBody modifier changes vertex count; cannot compare skin deformation")
        return {}

    results = {}
    for label, parent, child in JOINTS:
        if parent not in rig.pose.bones or child not in rig.pose.bones:
            issue(report, "errors", f"{label}: missing {parent} or {child} bone")
            continue
        mixed = [i for i, vertex in enumerate(weights)
                 if vertex.get(parent, 0) >= .15 and vertex.get(child, 0) >= .15]
        if len(mixed) < 6:
            issue(report, "errors", f"{label}: {len(mixed)} vertices blend {parent}/{child} (need at least 6)")
            continue

        best = (0.0, None, None)
        for action in actions.values():
            if action.name not in CLIPS:
                continue
            first, last = action.frame_range
            for fraction in (.25, .40, .55, .70, .85):
                frame = first + (last - first) * fraction
                assign_action(rig, action, frame)
                angle = joint_angle(rig, parent, child)
                if angle > best[0]:
                    best = (angle, action.name, frame)
        angle, clip, frame = best
        if clip is None or angle < .12:
            issue(report, "errors", f"{label}: joint never bends at least 0.12 rad in any clip")
            results[label] = {"blendedVertices": len(mixed), "maxJointAngleRad": round(angle, 4)}
            continue

        assign_action(rig, actions[clip], frame)
        posed = evaluated_positions(obj)
        parent_pose = rig.pose.bones[parent].matrix
        parent_rest = rig.pose.bones[parent].bone.matrix_local
        rig_inv = rig.matrix_world.inverted_safe()
        shift = max((parent_pose.inverted_safe() @ (rig_inv @ posed[i]) -
                     parent_rest.inverted_safe() @ (rig_inv @ rest[i])).length for i in mixed)
        # An articulated joint must move the shared skin relative to its parent.
        if shift < .012:
            issue(report, "errors", f"{label}: mixed skin moves only {shift:.4f} m in parent space")

        mixed_set = set(mixed)
        stretch = []
        for edge in obj.data.edges:
            a, b = edge.vertices
            if a not in mixed_set and b not in mixed_set:
                continue
            before = (rest[a] - rest[b]).length
            if before > 1e-5:
                stretch.append((posed[a] - posed[b]).length / before)
        minimum = min(stretch, default=1.0)
        maximum = max(stretch, default=1.0)
        if minimum < .20 or maximum > 3.0:
            issue(report, "errors", f"{label}: adjacent edge length ratio {minimum:.2f}..{maximum:.2f}")
        results[label] = {
            "blendedVertices": len(mixed), "clip": clip,
            "frame": round(frame, 2), "maxJointAngleRad": round(angle, 4),
            "skinTravelRelativeToParentM": round(shift, 4),
            "edgeLengthRatio": [round(minimum, 3), round(maximum, 3)],
        }
    return results


def audit(path: Path, manifest: dict) -> dict:
    report = {"file": str(path), "errors": [], "warnings": []}
    try:
        open_asset(path)
    except Exception as exc:
        issue(report, "errors", f"Cannot load asset: {type(exc).__name__}: {exc}")
        return report

    rig = bpy.data.objects.get("FinikRig")
    body = bpy.data.objects.get("PolygonBody")
    if rig is None or rig.type != "ARMATURE":
        issue(report, "errors", "FinikRig armature is absent")
        return report
    if body is None or body.type != "MESH":
        issue(report, "errors", "PolygonBody mesh is absent; the body cannot be verified as continuous")
        return report

    report["topology"] = topology(body.data, weld_export_seams=path.suffix == ".glb")
    mesh_topology = report["topology"]
    if len(mesh_topology["components"]) != 1:
        issue(report, "errors", f"PolygonBody has {len(mesh_topology['components'])} connected components")
    for key in ("boundaryEdges", "nonManifoldEdges", "looseVertices", "zeroAreaFaces"):
        if mesh_topology[key]:
            issue(report, "errors", f"PolygonBody has {mesh_topology[key]} {key}")
    if abs(mesh_topology["signedVolume"]) < .01:
        issue(report, "errors", "PolygonBody has near-zero enclosed volume")

    for obj in bpy.data.objects:
        if obj.type != "MESH":
            continue
        # Blender's glTF importer creates a bone-shape helper in this internal
        # collection. It is not a node of the exported GLB or part of the pet.
        if any(collection.name == "glTF_not_exported" for collection in obj.users_collection):
            continue
        armatures = [modifier for modifier in obj.modifiers if modifier.type == "ARMATURE"]
        if len(armatures) != 1 or armatures[0].object != rig:
            issue(report, "errors", f"{obj.name} must have one Armature modifier bound to FinikRig")
        if any(not vertex.groups for vertex in obj.data.vertices):
            issue(report, "errors", f"{obj.name} has unweighted vertices")
    weights, report["weights"] = vertex_weights(body, rig, report)

    for name in ACCESSORIES:
        obj = bpy.data.objects.get(name)
        if obj is None or obj.type != "MESH":
            issue(report, "errors", f"Missing accessory mesh {name}")
            continue
        modifiers = [modifier for modifier in obj.modifiers if modifier.type == "ARMATURE"]
        if len(modifiers) != 1 or modifiers[0].object != rig:
            issue(report, "errors", f"{name} is not skinned to FinikRig")
        expected_bone = "Head" if name.endswith("Cap") else "Chest"
        used_bones = {obj.vertex_groups[assignment.group].name
                      for vertex in obj.data.vertices for assignment in vertex.groups
                      if assignment.weight > .01}
        if expected_bone not in used_bones:
            issue(report, "errors", f"{name} has no weight on {expected_bone}")
        if not used_bones or not used_bones <= {bone.name for bone in rig.data.bones}:
            issue(report, "errors", f"{name} has missing or invalid skin weights")
    for name in ("PropFood", "PropBottle", "PropToy", "FoodSocket"):
        if name not in rig.data.bones:
            issue(report, "errors", f"Missing prop/socket bone {name}")

    present_materials = {material.name for material in bpy.data.materials}
    for name in MATERIALS:
        if name not in present_materials:
            issue(report, "errors", f"Missing customizable material {name}")
    report["materials"] = sorted(present_materials & set(MATERIALS))

    actions = {action.name: action for action in bpy.data.actions}
    report["clips"] = sorted(actions)
    for name in CLIPS:
        if name not in actions:
            issue(report, "errors", f"Missing animation clip {name}")
    expected_duration = {clip["name"]: clip["durationSeconds"] for clip in manifest.get("clips", [])}
    fps = bpy.context.scene.render.fps / bpy.context.scene.render.fps_base
    for name in set(CLIPS) & set(actions) & set(expected_duration):
        first, last = actions[name].frame_range
        actual = (last - first) / fps
        if abs(actual - expected_duration[name]) > .15:
            issue(report, "warnings", f"{name} duration {actual:.2f}s differs from manifest {expected_duration[name]:.2f}s")
    if "Sad" in actions:
        accessory_motion = {}
        for name in ACCESSORIES:
            obj = bpy.data.objects.get(name)
            if obj is None or obj.type != "MESH":
                continue
            rig.data.pose_position = "REST"
            bpy.context.view_layer.update()
            rest = evaluated_positions(obj)
            rig.data.pose_position = "POSE"
            first, last = actions["Sad"].frame_range
            assign_action(rig, actions["Sad"], first + (last - first) * .5)
            posed = evaluated_positions(obj)
            travel = max(((a - b).length for a, b in zip(rest, posed)), default=0.0)
            accessory_motion[name] = round(travel, 4)
            if travel < .01:
                issue(report, "errors", f"{name} does not follow the Sad animation ({travel:.4f} m travel)")
        report["accessoryMotionM"] = accessory_motion
    if len(weights) == len(body.data.vertices) and all(name in actions for name in CLIPS):
        report["deformation"] = audit_deformation(body, rig, weights, report)
    return report


def main() -> int:
    arguments = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    if not arguments:
        print("Usage: Blender --background --python tools/pets/validate_mesh.py -- <model-directory>")
        return 2
    root = Path(arguments[0]).expanduser().resolve()
    if not root.is_dir():
        print(f"Model directory does not exist: {root}")
        return 2
    manifest_path = root / "manifest.json"
    manifest = json.loads(manifest_path.read_text()) if manifest_path.exists() else {}
    files = [root / f"{species}{suffix}" for species in SPECIES for suffix in (".blend", ".glb")
             if (root / f"{species}{suffix}").exists()]
    if not files:
        print(f"No cat/owl/dog .blend or .glb files in {root}")
        return 2
    reports = []
    for path in files:
        try:
            reports.append(audit(path, manifest))
        except Exception as exc:
            reports.append({"file": str(path), "errors": [f"Audit failed: {type(exc).__name__}: {exc}"], "warnings": []})
    present = {path.stem for path in files}
    if set(SPECIES) - present:
        reports.append({"file": str(root), "errors": [f"Missing species: {sorted(set(SPECIES) - present)}"], "warnings": []})
    output = {"assets": reports, "errors": sum(len(report["errors"]) for report in reports),
              "warnings": sum(len(report["warnings"]) for report in reports)}
    print("FINIK_MESH_REPORT " + json.dumps(output, ensure_ascii=False, separators=(",", ":")))
    return 1 if output["errors"] else 0


if __name__ == "__main__":
    raise SystemExit(main())
