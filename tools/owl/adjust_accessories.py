"""Apply the owl accessory fit used by the Android app.

Only glTF node transforms are changed. The embedded meshes, textures, animation
data, and BIN chunk are copied byte-for-byte.
"""

import argparse
import json
import struct
from pathlib import Path


GLB_HEADER = struct.Struct("<III")
CHUNK_HEADER = struct.Struct("<II")
GLB_MAGIC = 0x46546C67
JSON_CHUNK = 0x4E4F534A

# glTF faces +Z; +Y is up. These are absolute local transforms under the
# existing Head/Spine bones, so rerunning this script does not move them again.
FITTING = {
    "Accessory_hat": {"translation": {1: 0.72, 2: -0.04}},
    "Accessory_medal": {"translation": {2: 0.29}, "scale": [0.33, 0.33, 0.33]},
    "Accessory_backpack": {"translation": {2: -0.42}},
}


def adjust(path: Path) -> None:
    data = path.read_bytes()
    magic, version, declared_length = GLB_HEADER.unpack_from(data)
    if magic != GLB_MAGIC or version != 2 or declared_length != len(data):
        raise ValueError(f"Invalid glTF 2.0 binary: {path}")

    chunks = []
    offset = GLB_HEADER.size
    while offset < len(data):
        length, kind = CHUNK_HEADER.unpack_from(data, offset)
        offset += CHUNK_HEADER.size
        chunks.append((kind, data[offset:offset + length]))
        offset += length
    if offset != len(data) or not chunks or chunks[0][0] != JSON_CHUNK:
        raise ValueError("Invalid GLB chunk layout")

    gltf = json.loads(chunks[0][1])
    for name, changes in FITTING.items():
        matching = [node for node in gltf["nodes"] if node.get("name") == name]
        if len(matching) != 1:
            raise ValueError(f"Expected one node named {name}, found {len(matching)}")
        node = matching[0]
        translation = node["translation"]
        for axis, value in changes["translation"].items():
            translation[axis] = value
        if "scale" in changes:
            node["scale"] = changes["scale"]

    encoded = json.dumps(gltf, ensure_ascii=False, separators=(",", ":"), allow_nan=False).encode("utf-8")
    chunks[0] = (JSON_CHUNK, encoded + b" " * (-len(encoded) % 4))
    body = b"".join(CHUNK_HEADER.pack(len(payload), kind) + payload for kind, payload in chunks)
    path.write_bytes(GLB_HEADER.pack(GLB_MAGIC, 2, GLB_HEADER.size + len(body)) + body)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("glb", type=Path, help="Android owl GLB to adjust in place")
    adjust(parser.parse_args().glb)
