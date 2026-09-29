"""Attach one shared food mesh to both owl hand tips and bake an eating shrink.

The source owl's BIN bytes and animation channels remain intact. The tool writes
only a distinct, new output file. Food is attached through fit nodes so its
original TRS or matrix is preserved exactly. Runtime must hide Food_Left/Right
outside Eating, including before the first visible frame.
"""

import argparse
import copy
import json
import math
import struct
from pathlib import Path

GLB_MAGIC = 0x46546C67
JSON_CHUNK = 0x4E4F534A
BIN_CHUNK = 0x004E4942
EATING_SOURCE = "01a0cfcc-7a03-75ac-bde5-e71cb8b79d9e"
EATING_NAME = "Eating"
FOOD_NODES = ("Food_Left", "Food_Right")
HAND_TIPS = ("mixamorig:LeftHandMiddle4", "mixamorig:RightHandMiddle4")
DEFAULT_SCALE = 0.16
FINAL_SCALE_FACTOR = 0.001
# glTF XYZW quaternions fitted so the bowl openings face upward during Eating.
LEFT_ROTATION = (-0.458076477, -0.273266852, -0.517941415, 0.668750942)
RIGHT_ROTATION = (-0.451953709, 0.222489044, 0.512441635, 0.695442379)


def read_glb(path):
    data = Path(path).read_bytes()
    if len(data) < 20 or struct.unpack_from("<III", data) != (GLB_MAGIC, 2, len(data)):
        raise ValueError(f"Invalid glTF 2.0 binary: {path}")
    chunks, offset = [], 12
    while offset < len(data):
        if offset + 8 > len(data):
            raise ValueError("Truncated GLB chunk header")
        length, kind = struct.unpack_from("<II", data, offset)
        offset += 8
        if length % 4 or offset + length > len(data):
            raise ValueError("Invalid GLB chunk length")
        chunks.append((kind, data[offset:offset + length]))
        offset += length
    if [kind for kind, _ in chunks] != [JSON_CHUNK, BIN_CHUNK]:
        raise ValueError("Expected exactly one JSON and one embedded BIN chunk")
    document = json.loads(chunks[0][1])
    buffers = document.get("buffers", [])
    if len(buffers) != 1 or "uri" in buffers[0]:
        raise ValueError("Expected exactly one embedded buffer")
    declared = buffers[0].get("byteLength", -1)
    if not 0 <= len(chunks[1][1]) - declared <= 3:
        raise ValueError("BIN chunk length does not match its buffer")
    for view in document.get("bufferViews", []):
        start, length = view.get("byteOffset", 0), view.get("byteLength", -1)
        if view.get("buffer") != 0 or start < 0 or length < 0 or start + length > declared:
            raise ValueError("Invalid bufferView range")
    return document, chunks[1][1]


def encode_glb(document, binary):
    encoded = json.dumps(document, ensure_ascii=False, separators=(",", ":"), allow_nan=False).encode()
    encoded += b" " * (-len(encoded) % 4)
    binary += b"\0" * (-len(binary) % 4)
    body = struct.pack("<II", len(encoded), JSON_CHUNK) + encoded
    body += struct.pack("<II", len(binary), BIN_CHUNK) + binary
    return struct.pack("<III", GLB_MAGIC, 2, 12 + len(body)) + body


def _reject_extensions(value):
    if isinstance(value, dict):
        for key, child in value.items():
            if key in ("extensions", "extensionsUsed", "extensionsRequired") and child:
                raise ValueError(f"Unsupported food glTF extensions: {child}")
            if key != "extras":
                _reject_extensions(child)
    elif isinstance(value, list):
        for child in value:
            _reject_extensions(child)


def _single_named(items, name):
    matching = [index for index, item in enumerate(items) if item.get("name") == name]
    if len(matching) != 1:
        raise ValueError(f"Expected exactly one {name}, found {len(matching)}")
    return matching[0]


def _offset_index(value, count, offset, description):
    if not isinstance(value, int) or isinstance(value, bool) or not 0 <= value < count:
        raise ValueError(f"Invalid {description} index: {value}")
    return value + offset


def merge(owl, owl_binary, food, food_binary, *, scale=DEFAULT_SCALE,
          translation=(0.0, 0.0, 0.0), left_rotation=LEFT_ROTATION, right_rotation=RIGHT_ROTATION):
    """Return a new document/BIN, without mutating either source document."""
    if not math.isfinite(scale) or scale <= 0:
        raise ValueError("Food scale must be finite and positive")
    for rotation in (left_rotation, right_rotation):
        if len(translation) != 3 or len(rotation) != 4 or not all(
            math.isfinite(x) for x in (*translation, *rotation)
        ):
            raise ValueError("Invalid food fitting transform")
        if abs(sum(x * x for x in rotation) - 1) > 1e-5:
            raise ValueError("Food fitting rotation must be a unit quaternion")
    result = copy.deepcopy(owl)
    if any(node.get("name", "").startswith("Food_") for node in result.get("nodes", [])):
        raise ValueError("Owl already contains Food_ nodes; use the original owl")
    hands = [_single_named(result["nodes"], name) for name in HAND_TIPS]
    eating_index = _single_named(result.get("animations", []), EATING_SOURCE)
    if any(animation.get("name") == EATING_NAME for animation in result["animations"]):
        raise ValueError("Owl already contains an Eating animation")
    _reject_extensions(food)
    if food.get("skins") or food.get("animations") or food.get("cameras"):
        raise ValueError("Food must be static, without skins, animations or cameras")
    if len(food.get("nodes", [])) != 1 or len(food.get("meshes", [])) != 1:
        raise ValueError("Food must have exactly one mesh and one node; bake its scene first")
    source_node = food["nodes"][0]
    if source_node.get("mesh") != 0 or source_node.get("children") or "skin" in source_node:
        raise ValueError("Food must contain one static mesh node")
    if "matrix" in source_node and any(key in source_node for key in ("translation", "rotation", "scale")):
        raise ValueError("Food node cannot combine matrix and TRS")
    offsets = {key: len(result.get(key, [])) for key in (
        "bufferViews", "accessors", "images", "samplers", "textures", "materials", "meshes"
    )}
    def remap(value, key):
        return _offset_index(value, len(food.get(key, [])), offsets[key], key)
    binary = bytearray(owl_binary)
    binary.extend(b"\0" * (-len(binary) % 4))
    food_offset = len(binary)
    binary.extend(food_binary)
    for key in offsets:
        result.setdefault(key, [])
    for original in food.get("bufferViews", []):
        item = copy.deepcopy(original)
        if item.get("buffer") != 0:
            raise ValueError("Food bufferView must use embedded buffer 0")
        item["byteOffset"] = item.get("byteOffset", 0) + food_offset
        result["bufferViews"].append(item)
    for original in food.get("accessors", []):
        item = copy.deepcopy(original)
        if "bufferView" in item:
            item["bufferView"] = remap(item["bufferView"], "bufferViews")
        if "sparse" in item:
            for field in ("indices", "values"):
                sparse = item["sparse"][field]
                sparse["bufferView"] = remap(sparse["bufferView"], "bufferViews")
        result["accessors"].append(item)
    for original in food.get("images", []):
        item = copy.deepcopy(original)
        if "uri" in item or "bufferView" not in item:
            raise ValueError("Food images must be embedded bufferViews")
        item["bufferView"] = remap(item["bufferView"], "bufferViews")
        result["images"].append(item)
    result["samplers"].extend(copy.deepcopy(food.get("samplers", [])))
    for original in food.get("textures", []):
        item = copy.deepcopy(original)
        item["source"] = remap(item["source"], "images")
        if "sampler" in item:
            item["sampler"] = remap(item["sampler"], "samplers")
        result["textures"].append(item)
    for original in food.get("materials", []):
        item = copy.deepcopy(original)
        # Avoid colliding with the owl's BakedMaterial color-swapping target.
        item["name"] = "Food_" + item.get("name", "Material")
        owners = [(item, ("normalTexture", "occlusionTexture", "emissiveTexture")),
                  (item.get("pbrMetallicRoughness", {}), ("baseColorTexture", "metallicRoughnessTexture"))]
        for owner, fields in owners:
            for field in fields:
                if field in owner:
                    owner[field]["index"] = remap(owner[field]["index"], "textures")
        result["materials"].append(item)
    mesh = copy.deepcopy(food["meshes"][0])
    mesh["name"] = "Food_SharedMesh"
    for primitive in mesh["primitives"]:
        primitive["attributes"] = {name: remap(index, "accessors") for name, index in primitive["attributes"].items()}
        for target in primitive.get("targets", []):
            for name, index in target.items():
                target[name] = remap(index, "accessors")
        if "indices" in primitive:
            primitive["indices"] = remap(primitive["indices"], "accessors")
        if "material" in primitive:
            primitive["material"] = remap(primitive["material"], "materials")
    result["meshes"].append(mesh)
    anchors = []
    for name, hand, rotation in zip(FOOD_NODES, hands, (left_rotation, right_rotation)):
        anchor_index = len(result["nodes"])
        anchors.append(anchor_index)
        result["nodes"].append({"name": name + "_Attachment", "translation": list(translation),
                                "rotation": list(rotation), "scale": [scale] * 3,
                                "children": [anchor_index + 1]})
        attached = copy.deepcopy(source_node)
        attached.update(name=name, mesh=offsets["meshes"])
        result["nodes"].append(attached)
        result["nodes"][hand].setdefault("children", []).append(anchor_index)
    eating = result["animations"][eating_index]
    eating["name"] = EATING_NAME
    # Read actual input times: accessor max metadata is optional in glTF animations.
    duration = max(_float_scalar_max(owl, owl_binary, sampler["input"]) for sampler in eating["samplers"])
    if not math.isfinite(duration) or duration <= 0:
        raise ValueError("Eating clip must have a positive finite duration")
    def append_floats(values, kind, count, minimum=None, maximum=None):
        binary.extend(b"\0" * (-len(binary) % 4))
        view_index = len(result["bufferViews"])
        result["bufferViews"].append({"buffer": 0, "byteOffset": len(binary), "byteLength": 4 * len(values)})
        binary.extend(struct.pack("<" + "f" * len(values), *values))
        item = {"bufferView": view_index, "componentType": 5126, "count": count, "type": kind}
        if minimum is not None:
            item["min"], item["max"] = minimum, maximum
        index = len(result["accessors"])
        result["accessors"].append(item)
        return index
    timeline = [0.0, duration * 0.18, duration * 0.9, duration]
    input_index = append_floats(timeline, "SCALAR", 4, [0.0], [duration])
    output_index = append_floats([scale] * 6 + [scale * FINAL_SCALE_FACTOR] * 6, "VEC3", 4)
    sampler_index = len(eating["samplers"])
    eating["samplers"].append({"input": input_index, "output": output_index, "interpolation": "LINEAR"})
    eating["channels"].extend({"sampler": sampler_index, "target": {"node": anchor, "path": "scale"}} for anchor in anchors)
    result["buffers"][0]["byteLength"] = len(binary)
    return result, bytes(binary)


def _float_scalar_max(document, binary, index):
    accessor = document["accessors"][index]
    if accessor.get("componentType") != 5126 or accessor.get("type") != "SCALAR" or "sparse" in accessor:
        raise ValueError("Eating timeline must be a dense FLOAT SCALAR accessor")
    view = document["bufferViews"][accessor["bufferView"]]
    start = view.get("byteOffset", 0) + accessor.get("byteOffset", 0)
    stride = view.get("byteStride", 4)
    count = accessor["count"]
    if count < 1 or start + (count - 1) * stride + 4 > len(binary):
        raise ValueError("Invalid eating timeline buffer range")
    return max(struct.unpack_from("<f", binary, start + i * stride)[0] for i in range(count))


def create_asset(owl_path, food_path, output_path, **fit):
    owl_path, food_path, output_path = map(Path, (owl_path, food_path, output_path))
    if output_path.resolve() in (owl_path.resolve(), food_path.resolve()):
        raise ValueError("Output must differ from both inputs; source assets are never overwritten")
    if output_path.exists():
        raise ValueError("Output already exists; choose a new output path")
    owl, owl_binary = read_glb(owl_path)
    food, food_binary = read_glb(food_path)
    document, binary = merge(owl, owl_binary, food, food_binary, **fit)
    # Exclusive creation prevents an existing output being replaced between checks.
    with output_path.open("xb") as output:
        output.write(encode_glb(document, binary))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--owl", type=Path, required=True)
    parser.add_argument("--food", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--scale", type=float, default=DEFAULT_SCALE)
    parser.add_argument("--translation", type=float, nargs=3, default=(0, 0, 0), metavar=("X", "Y", "Z"))
    parser.add_argument("--left-rotation", type=float, nargs=4, default=LEFT_ROTATION, metavar=("X", "Y", "Z", "W"))
    parser.add_argument("--right-rotation", type=float, nargs=4, default=RIGHT_ROTATION, metavar=("X", "Y", "Z", "W"))
    args = parser.parse_args()
    try:
        create_asset(args.owl, args.food, args.output, scale=args.scale, translation=args.translation, left_rotation=args.left_rotation, right_rotation=args.right_rotation)
    except (ValueError, OSError, KeyError, IndexError, struct.error) as error:
        parser.exit(2, f"Cannot attach food: {error}\n")
    print(f"Created {args.output} ({args.output.stat().st_size:,} bytes)")


if __name__ == "__main__":
    main()
