"""Prepare the Meshy berries-and-nuts GLB for the owl's small hand props.

Run in an isolated Blender background scene, for example:

    blender --background --factory-startup --python tools/owl/prepare_food.py -- \
        --source /path/to/Meshy_food.glb --output /tmp/food-mobile.glb

The source file is never changed. The default settings retain about 14,000
triangles from the supplied asset and use 1024 px textures. The resulting GLB
keeps glTF's original coordinate system for add_feeding_food.py.
"""

import argparse
from pathlib import Path
import sys

import bpy


def arguments():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--ratio", type=float, default=0.055)
    parser.add_argument("--texture-size", type=int, default=1024)
    parser.add_argument("--jpeg-quality", type=int, default=88)
    argv = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    args = parser.parse_args(argv)
    if args.source.resolve() == args.output.resolve():
        parser.error("Output must differ from the original source file")
    if not args.source.is_file():
        parser.error(f"Source GLB does not exist: {args.source}")
    if args.output.suffix.lower() != ".glb":
        parser.error("Output must have the .glb extension")
    if not 0 < args.ratio <= 1:
        parser.error("Ratio must be greater than 0 and at most 1")
    if args.texture_size < 1 or not 1 <= args.jpeg_quality <= 100:
        parser.error("Texture size must be positive; JPEG quality must be 1–100")
    return args


def main():
    args = arguments()
    bpy.ops.object.select_all(action="SELECT")
    bpy.ops.object.delete(use_global=False)
    bpy.ops.import_scene.gltf(filepath=str(args.source.resolve()))
    meshes = [obj for obj in bpy.data.objects if obj.type == "MESH"]
    if len(meshes) != 1:
        raise ValueError("Expected the supplied single-mesh berries-and-nuts GLB")
    obj = meshes[0]
    bpy.ops.object.select_all(action="DESELECT")
    bpy.context.view_layer.objects.active = obj
    obj.select_set(True)

    # glTF duplicates vertices at UV seams. Welding geometry before decimation
    # preserves the smooth fruit silhouettes; per-face UVs remain independent.
    bpy.ops.object.mode_set(mode="EDIT")
    bpy.ops.mesh.select_all(action="SELECT")
    bpy.ops.mesh.remove_doubles(threshold=0.00001)
    bpy.ops.object.mode_set(mode="OBJECT")
    modifier = obj.modifiers.new("MobileMesh", "DECIMATE")
    modifier.ratio = args.ratio
    bpy.ops.object.modifier_apply(modifier=modifier.name)
    # Imported split normals describe the high-resolution mesh and cause visible
    # triangular shading after decimation. Zero entries request automatic normals.
    obj.data.normals_split_custom_set([(0, 0, 0)] * len(obj.data.loops))
    for polygon in obj.data.polygons:
        polygon.use_smooth = True

    for image in bpy.data.images:
        width, height = image.size
        if max(width, height) > args.texture_size:
            factor = args.texture_size / max(width, height)
            image.scale(max(1, round(width * factor)), max(1, round(height * factor)))
            image.pack()

    args.output.parent.mkdir(parents=True, exist_ok=True)
    bpy.ops.export_scene.gltf(
        filepath=str(args.output.resolve()),
        export_format="GLB",
        export_image_format="JPEG",
        export_image_quality=args.jpeg_quality,
        use_selection=True,
        export_animations=False,
    )
    print(f"Prepared {len(obj.data.polygons):,} faces: {args.output}")
    print(f"GLB size: {args.output.stat().st_size:,} bytes")


if __name__ == "__main__":
    main()
