"""Append one compatible skeletal animation without replacing the current owl.

Only the selected clip's bufferViews/accessors are imported. Existing meshes,
materials, accessories, feeding channels, and BIN bytes remain intact. This is
an exact-rig transfer, not a retargeter: named bones and their ancestors must
have matching parents and local transforms. The output must be a new file.
"""

import argparse
import copy
import math
import struct
from pathlib import Path

from add_feeding_food import encode_glb, read_glb

DEFAULT_CLIP = "FunnyDancing_01"
TRANSFORM_TOLERANCE = 1e-5
BOTTLE_NAME = "Prop_Bottle"


def _index(items, index, description):
    if not isinstance(index, int) or isinstance(index, bool) or not 0 <= index < len(items):
        raise ValueError(f"Invalid {description} index: {index}")
    return items[index]


def _named(items, name):
    matches = [i for i, item in enumerate(items) if item.get("name") == name]
    if not name or len(matches) != 1:
        raise ValueError(f"Expected exactly one named {name!r}, found {len(matches)}")
    return matches[0]


def _parents(nodes):
    parents = {}
    for parent, node in enumerate(nodes):
        for child in node.get("children", []):
            _index(nodes, child, "child node")
            if child in parents:
                raise ValueError("A node has multiple parents")
            parents[child] = parent
    return parents


def _vector(node, key, default):
    value = node.get(key, default)
    if len(value) != len(default) or not all(isinstance(x, (int, float)) and math.isfinite(x) for x in value):
        raise ValueError(f"Invalid {key} on {node.get('name')}")
    return value


def _same_transform(source, target):
    # A matrix and TRS are deliberately not interconverted: animated glTF nodes
    # must use TRS, and silently accepting a different basis would be retargeting.
    if "matrix" in source or "matrix" in target:
        identity = [1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1]
        if any(key in node for node in (source, target) for key in ("translation", "rotation", "scale")):
            return False
        pairs = [(_vector(source, "matrix", identity), _vector(target, "matrix", identity))]
    else:
        pairs = [(_vector(source, key, default), _vector(target, key, default))
                 for key, default in (("translation", [0, 0, 0]), ("scale", [1, 1, 1]))]
        a, b = (_vector(node, "rotation", [0, 0, 0, 1]) for node in (source, target))
        # q and -q describe the same orientation.
        if sum(x * y for x, y in zip(a, b)) < 0:
            b = [-x for x in b]
        pairs.append((a, b))
    return all(abs(x - y) <= TRANSFORM_TOLERANCE for a, b in pairs for x, y in zip(a, b))


def _compatible_nodes(owl, source, animated):
    src_nodes, dst_nodes = source.get("nodes", []), owl.get("nodes", [])
    src_parents, dst_parents = _parents(src_nodes), _parents(dst_nodes)
    remapped = {}
    for start in animated:
        seen, current = set(), start
        while current is not None:
            if current in seen:
                raise ValueError("Source rig contains a parent cycle")
            seen.add(current)
            src_node = _index(src_nodes, current, "animation node")
            name = src_node.get("name")
            _named(src_nodes, name)
            target = _named(dst_nodes, name)
            dst_node = dst_nodes[target]
            if not _same_transform(src_node, dst_node):
                raise ValueError(f"Incompatible local transform: {name}")
            src_parent, dst_parent = src_parents.get(current), dst_parents.get(target)
            src_name = src_nodes[src_parent].get("name") if src_parent is not None else None
            dst_name = dst_nodes[dst_parent].get("name") if dst_parent is not None else None
            if src_name != dst_name or (src_parent is None) != (dst_parent is None):
                raise ValueError(f"Incompatible parent: {name}")
            remapped[current] = target
            current = src_parent
    return remapped


def _floats(document, binary, index, kind):
    accessor = _index(document.get("accessors", []), index, "accessor")
    if accessor.get("componentType") != 5126 or accessor.get("type") != kind or "sparse" in accessor:
        raise ValueError(f"Animation data must be dense FLOAT {kind}")
    if accessor.get("extensions") or accessor.get("normalized"):
        raise ValueError("Unsupported animation accessor encoding")
    view = _index(document.get("bufferViews", []), accessor.get("bufferView"), "bufferView")
    width = {"SCALAR": 1, "VEC3": 3, "VEC4": 4}[kind]
    count, offset = accessor.get("count", 0), accessor.get("byteOffset", 0)
    stride = view.get("byteStride", width * 4)
    start, length = view.get("byteOffset", 0), view.get("byteLength", -1)
    if (view.get("buffer") != 0 or view.get("extensions") or count < 1 or offset < 0 or
            stride < width * 4 or stride % 4 or offset % 4 or start < 0 or
            offset + (count - 1) * stride + width * 4 > length or start + length > len(binary)):
        raise ValueError("Invalid animation buffer range")
    rows = [struct.unpack_from("<" + "f" * width, binary, start + offset + i * stride) for i in range(count)]
    if not all(math.isfinite(value) for row in rows for value in row):
        raise ValueError("Animation contains non-finite values")
    return rows


def merge(owl, owl_binary, source, source_binary, *, clip=DEFAULT_CLIP):
    """Return a deterministic new document/BIN without mutating either input."""
    if any(animation.get("name") == clip for animation in owl.get("animations", [])):
        raise ValueError(f"Owl already contains animation {clip!r}")
    original = source.get("animations", [])[_named(source.get("animations", []), clip)]
    if original.get("extensions") or not original.get("channels"):
        raise ValueError("Animation must have ordinary skeletal channels")
    targets, needed_samplers, duration = set(), set(), 0.0
    for channel in original["channels"]:
        target = channel["target"]
        if channel.get("extensions") or target.get("extensions") or target.get("path") not in ("rotation", "translation", "scale"):
            raise ValueError("Only ordinary skeletal TRS animation channels are supported")
        node = _index(source.get("nodes", []), target.get("node"), "animation node")
        if "matrix" in node:
            raise ValueError("Animated nodes must use TRS, not matrices")
        key = (target["node"], target["path"])
        if key in targets:
            raise ValueError("Duplicate animation channel target")
        targets.add(key)
        sampler_index = channel["sampler"]
        sampler = _index(original.get("samplers", []), sampler_index, "animation sampler")
        interpolation = sampler.get("interpolation", "LINEAR")
        if sampler.get("extensions") or interpolation not in ("LINEAR", "STEP", "CUBICSPLINE"):
            raise ValueError("Unsupported animation interpolation")
        times = [row[0] for row in _floats(source, source_binary, sampler["input"], "SCALAR")]
        if times[0] < 0 or any(a >= b for a, b in zip(times, times[1:])):
            raise ValueError("Animation timeline must be nonnegative and strictly increasing")
        output = _floats(source, source_binary, sampler["output"], "VEC4" if target["path"] == "rotation" else "VEC3")
        if len(output) != len(times) * (3 if interpolation == "CUBICSPLINE" else 1):
            raise ValueError("Animation input/output key counts differ")
        duration = max(duration, times[-1])
        needed_samplers.add(sampler_index)
    if duration <= 0:
        raise ValueError("Animation must have a positive duration")
    node_map = _compatible_nodes(owl, source, sorted({node for node, _ in targets}))
    if any("matrix" in owl["nodes"][node_map[node]] for node, _ in targets):
        raise ValueError("Animated target nodes must use TRS, not matrices")
    result, binary = copy.deepcopy(owl), bytearray(owl_binary)
    for key in ("bufferViews", "accessors", "animations"):
        result.setdefault(key, [])
    accessor_map, view_map = {}, {}

    def copy_accessor(index):
        if index not in accessor_map:
            accessor = copy.deepcopy(source["accessors"][index])
            view_index = accessor["bufferView"]
            if view_index not in view_map:
                view = copy.deepcopy(source["bufferViews"][view_index])
                start, length = view.get("byteOffset", 0), view["byteLength"]
                binary.extend(b"\0" * (-len(binary) % 4))
                view["byteOffset"] = len(binary)
                binary.extend(source_binary[start:start + length])
                view_map[view_index] = len(result["bufferViews"])
                result["bufferViews"].append(view)
            accessor["bufferView"] = view_map[view_index]
            accessor_map[index] = len(result["accessors"])
            result["accessors"].append(accessor)
        return accessor_map[index]

    animation = copy.deepcopy(original)
    animation["samplers"] = []
    sampler_map = {}
    for index in sorted(needed_samplers):
        sampler = copy.deepcopy(original["samplers"][index])
        for field in ("input", "output"):
            sampler[field] = copy_accessor(sampler[field])
        sampler_map[index] = len(animation["samplers"])
        animation["samplers"].append(sampler)
    for channel in animation["channels"]:
        channel["target"]["node"] = node_map[channel["target"]["node"]]
        channel["sampler"] = sampler_map[channel["sampler"]]

    def append_floats(values, kind, count):
        binary.extend(b"\0" * (-len(binary) % 4))
        view_index = len(result["bufferViews"])
        result["bufferViews"].append({"buffer": 0, "byteOffset": len(binary), "byteLength": len(values) * 4})
        binary.extend(struct.pack("<" + "f" * len(values), *values))
        index = len(result["accessors"])
        accessor = {"bufferView": view_index, "componentType": 5126, "count": count, "type": kind}
        if kind == "SCALAR":
            accessor.update(min=[min(values)], max=[max(values)])
        result["accessors"].append(accessor)
        return index

    if any(node.get("name") == BOTTLE_NAME for node in result.get("nodes", [])):
        bottle = _named(result["nodes"], BOTTLE_NAME)
        if any(channel["target"]["node"] == bottle for channel in animation["channels"]):
            raise ValueError("Selected skeletal clip unexpectedly animates the bottle")
        # Reset the drinking prop immediately without extending the source clip.
        timeline = append_floats([0.0, duration], "SCALAR", 2)
        hidden = append_floats([0.001] * 6, "VEC3", 2)
        animation["channels"].append({"sampler": len(animation["samplers"]), "target": {"node": bottle, "path": "scale"}})
        animation["samplers"].append({"input": timeline, "output": hidden, "interpolation": "LINEAR"})
    result["animations"].append(animation)
    result["buffers"][0]["byteLength"] = len(binary)
    return result, bytes(binary)


def create_asset(owl_path, source_path, output_path, *, clip=DEFAULT_CLIP):
    owl_path, source_path, output_path = map(Path, (owl_path, source_path, output_path))
    if output_path.resolve() in (owl_path.resolve(), source_path.resolve()):
        raise ValueError("Output must differ from both inputs; source assets are never overwritten")
    if output_path.exists():
        raise ValueError("Output already exists; choose a new output path")
    owl, owl_binary = read_glb(owl_path)
    source, source_binary = read_glb(source_path)
    document, binary = merge(owl, owl_binary, source, source_binary, clip=clip)
    with output_path.open("xb") as output:
        output.write(encode_glb(document, binary))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--owl", type=Path, required=True)
    parser.add_argument("--source", type=Path, required=True)
    parser.add_argument("--clip", default=DEFAULT_CLIP)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    try:
        create_asset(args.owl, args.source, args.output, clip=args.clip)
    except (ValueError, OSError, KeyError, IndexError, TypeError, struct.error) as error:
        parser.exit(2, f"Cannot append animation: {error}\n")
    print(f"Created {args.output} ({args.output.stat().st_size:,} bytes)")


if __name__ == "__main__":
    main()
