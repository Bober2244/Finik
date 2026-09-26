"""Render a polygon topology and skeleton QA image from a Finik .blend file.

Usage:
    blender --background --factory-startup --python \
        tools/pets/render_wireframe.py -- cat.blend cat_wireframe.png

The wireframe is the actual mesh topology.  The red overlay projects the
stored armature's rest bones onto a plane just in front of the character, so
they remain legible even though most of the skeleton lies inside the skin.
No model source or exported GLB is changed.
"""

from __future__ import annotations

import math
import sys
from pathlib import Path

import bpy
from mathutils import Vector


def material(name, color, roughness=.8):
    result = bpy.data.materials.new(name)
    result.diffuse_color = (*color, 1)
    result.use_nodes = True
    shader = result.node_tree.nodes.get("Principled BSDF")
    shader.inputs["Base Color"].default_value = (*color, 1)
    shader.inputs["Roughness"].default_value = roughness
    return result


def add_wire_copy(source, wire_mat):
    overlay = source.copy()
    overlay.data = source.data.copy()
    overlay.name = source.name + "_QA_Wire"
    bpy.context.collection.objects.link(overlay)
    overlay.data.materials.clear()
    overlay.data.materials.append(wire_mat)
    modifier = overlay.modifiers.new("Actual polygon edges", "WIREFRAME")
    modifier.thickness = .0038 if source.name == "PolygonBody" else .0025
    modifier.offset = .75
    modifier.use_replace = True
    modifier.use_even_offset = True
    return overlay


def projected_bone_lines(rig, line_mat, joint_mat):
    verts = []
    faces = []
    joints = []
    plane_y = -.84
    width = .010
    skip = ("Prop", "FoodSocket")
    count = 0
    for bone in rig.data.bones:
        if bone.name.startswith(skip):
            continue
        head = rig.matrix_world @ bone.head_local
        tail = rig.matrix_world @ bone.tail_local
        a = Vector((head.x, head.z))
        b = Vector((tail.x, tail.z))
        direction = b-a
        if direction.length < 1e-5:
            continue
        perpendicular = Vector((-direction.y, direction.x)).normalized()*width
        origin = len(verts)
        for point in (a+perpendicular, a-perpendicular,
                      b-perpendicular, b+perpendicular):
            verts.append((point.x, plane_y, point.y))
        faces.append((origin, origin+1, origin+2, origin+3))
        joints.append((a.x, a.y))
        count += 1
    mesh = bpy.data.meshes.new("Projected actual armature bones")
    mesh.from_pydata(verts, [], faces)
    mesh.materials.append(line_mat)
    mesh.update()
    obj = bpy.data.objects.new("QA_Skeleton_Bones", mesh)
    bpy.context.collection.objects.link(obj)

    # A tiny eight-sided polygon marks each bone's actual head joint.
    dots = []
    dot_faces = []
    for x, z in joints:
        first = len(dots)
        dots.append((x, plane_y-.002, z))
        for i in range(8):
            theta = 2*math.pi*i/8
            dots.append((x+.019*math.cos(theta), plane_y-.002,
                         z+.019*math.sin(theta)))
        for i in range(8):
            dot_faces.append((first, first+1+i, first+1+(i+1)%8))
    dot_mesh = bpy.data.meshes.new("Projected armature joints")
    dot_mesh.from_pydata(dots, [], dot_faces)
    dot_mesh.materials.append(joint_mat)
    dot_mesh.update()
    joint_obj = bpy.data.objects.new("QA_Skeleton_Joints", dot_mesh)
    bpy.context.collection.objects.link(joint_obj)
    return count


def main():
    try:
        separator = sys.argv.index("--")
        blend_file, output_file = (Path(p).expanduser().resolve()
                                   for p in sys.argv[separator+1:separator+3])
    except (ValueError, IndexError):
        raise SystemExit("Usage: blender --background --python render_wireframe.py -- MODEL.blend OUTPUT.png")
    if not blend_file.is_file():
        raise SystemExit(f"Blend file does not exist: {blend_file}")
    output_file.parent.mkdir(parents=True, exist_ok=True)
    bpy.ops.wm.open_mainfile(filepath=str(blend_file))

    body = bpy.data.objects.get("PolygonBody")
    rig = bpy.data.objects.get("FinikRig")
    if body is None or body.type != "MESH":
        raise SystemExit("Expected mesh PolygonBody in the blend file")
    if rig is None or rig.type != "ARMATURE":
        raise SystemExit("Expected armature FinikRig in the blend file")

    for obj in bpy.data.objects:
        if obj.name.startswith(("Accessory_", "Prop_")):
            obj.hide_render = True
    body.hide_render = False
    details = bpy.data.objects.get("PetDetails")
    if details is not None:
        details.hide_render = False

    wire_mat = material("QA polygon edges", (.020, .075, .110))
    bone_mat = material("QA skeleton bones", (.96, .16, .085))
    joint_mat = material("QA skeleton joints", (1.0, .76, .19))
    add_wire_copy(body, wire_mat)
    if details is not None:
        add_wire_copy(details, wire_mat)
    bone_count = projected_bone_lines(rig, bone_mat, joint_mat)

    camera = bpy.data.objects.get("PreviewCamera")
    if camera is None:
        bpy.ops.object.camera_add()
        camera = bpy.context.object
    camera.location = (0, -7.4, 2.6)
    camera.rotation_euler = (Vector((0, 0, 1.34))-camera.location).to_track_quat('-Z','Y').to_euler()
    camera.data.type = "ORTHO"
    camera.data.ortho_scale = 3.22
    scene = bpy.context.scene
    scene.camera = camera
    scene.frame_set(0)
    scene.render.engine = 'BLENDER_EEVEE'
    scene.render.resolution_x = 1100
    scene.render.resolution_y = 1100
    scene.render.resolution_percentage = 100
    scene.render.image_settings.file_format = 'PNG'
    scene.render.film_transparent = True
    scene.render.filepath = str(output_file)
    bpy.ops.render.render(write_still=True)
    body.data.calc_loop_triangles()
    print(f"FINIK_WIREFRAME_QA {output_file} "
          f"body_vertices={len(body.data.vertices)} "
          f"body_triangles={len(body.data.loop_triangles)} "
          f"bones={bone_count}")


if __name__ == "__main__":
    main()
