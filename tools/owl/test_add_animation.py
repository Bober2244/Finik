"""Tests for transferring one clip while retaining the customized owl exactly."""

import copy
import struct
import tempfile
import unittest
from pathlib import Path

from add_animation import DEFAULT_CLIP, create_asset, merge
from add_feeding_food import encode_glb, read_glb


def fixtures():
    source = {
        "asset": {"version": "2.0"}, "buffers": [{"byteLength": 0}], "bufferViews": [], "accessors": [],
        # Deliberately different node order from the target; the root has no channel.
        "nodes": [{"name": "Tip", "translation": [0, 1, 0]},
                  {"name": "Root", "children": [2], "scale": [2, 2, 2]},
                  {"name": "Hip", "children": [0]}],
        "materials": [{"name": "DoNotImport"}], "meshes": [{"primitives": []}],
        "scenes": [{"nodes": [1]}], "scene": 0,
    }
    binary = bytearray()

    def floats(values, kind, count):
        view = len(source["bufferViews"])
        source["bufferViews"].append({"buffer": 0, "byteOffset": len(binary), "byteLength": len(values) * 4})
        binary.extend(struct.pack("<" + "f" * len(values), *values))
        index = len(source["accessors"])
        source["accessors"].append({"bufferView": view, "componentType": 5126, "count": count, "type": kind})
        return index

    # Include unrelated data before and between the selected clip's accessors.
    unused_time = floats([0, 100], "SCALAR", 2)
    time_a = floats([0, 1.5], "SCALAR", 2)
    rotations = floats([0, 0, 0, 1, 0, 0.6, 0, 0.8], "VEC4", 2)
    unused_output = floats([99] * 6, "VEC3", 2)
    time_b = floats([0, 0.75, 1.5], "SCALAR", 3)
    translations = floats([0, 0, 0, 0.3, 0.2, 0, -0.1, 0, 0], "VEC3", 3)
    source["animations"] = [
        {"name": "UnusedLongClip", "samplers": [{"input": unused_time, "output": unused_output}],
         "channels": [{"sampler": 0, "target": {"node": 2, "path": "translation"}}]},
        {"name": DEFAULT_CLIP, "extras": {"keep": True}, "samplers": [
            {"input": time_a, "output": rotations, "interpolation": "LINEAR"},
            {"input": unused_time, "output": unused_output},
            {"input": time_b, "output": translations, "interpolation": "STEP"}],
         "channels": [{"sampler": 2, "target": {"node": 2, "path": "translation"}},
                      {"sampler": 0, "target": {"node": 0, "path": "rotation"}}]},
    ]
    source["buffers"][0]["byteLength"] = len(binary)
    owl_binary = struct.pack("<2f", 0, 6) + b"ORIGINAL OWL FOOD TEXTURES"
    owl = {
        "asset": {"version": "2.0", "generator": "customized owl"},
        "buffers": [{"byteLength": len(owl_binary)}],
        "bufferViews": [{"buffer": 0, "byteOffset": 0, "byteLength": 8}],
        "accessors": [{"bufferView": 0, "componentType": 5126, "count": 2, "type": "SCALAR"}],
        "nodes": [{"name": "Root", "children": [1], "scale": [2, 2, 2]},
                  {"name": "Hip", "children": [2, 3, 4]},
                  {"name": "Tip", "translation": [0, 1, 0]},
                  {"name": "Prop_Bottle", "scale": [0.001] * 3, "mesh": 1},
                  {"name": "Food_Left", "mesh": 2}],
        "meshes": [{"name": "Body"}, {"name": "Bottle"}, {"name": "Food"}],
        "materials": [{"name": "BakedMaterial"}, {"name": "Food_Material"}],
        "images": [{"bufferView": 0, "mimeType": "image/png"}],
        "textures": [{"source": 0}], "skins": [{"joints": [1, 2]}],
        "animations": [{"name": "Eating", "extras": {"shrinkingFood": True},
                        "samplers": [{"input": 0, "output": 0}],
                        "channels": [{"sampler": 0, "target": {"node": 4, "path": "scale"}}]}],
        "scenes": [{"nodes": [0]}], "scene": 0,
    }
    return owl, owl_binary, source, bytes(binary)


def values(document, binary, index):
    accessor = document["accessors"][index]
    view = document["bufferViews"][accessor["bufferView"]]
    width = {"SCALAR": 1, "VEC3": 3, "VEC4": 4}[accessor["type"]]
    return struct.unpack_from("<" + "f" * width * accessor["count"], binary,
                              view.get("byteOffset", 0) + accessor.get("byteOffset", 0))


class AnimationTransferTests(unittest.TestCase):
    def test_preserves_existing_owl_and_imports_no_geometry(self):
        inputs = fixtures()
        snapshot = copy.deepcopy(inputs)
        owl, old_binary, _, _ = inputs
        result, binary = merge(*inputs)
        self.assertEqual(inputs, snapshot)
        self.assertEqual(binary[:len(old_binary)], old_binary)
        for key, value in owl.items():
            if key == "buffers":
                continue
            if key in ("bufferViews", "accessors", "animations"):
                self.assertEqual(result[key][:len(value)], value)
            else:
                self.assertEqual(result[key], value)
        self.assertEqual(len(result["animations"]), 2)
        self.assertEqual(len(result["accessors"]), len(owl["accessors"]) + 4 + 2)
        self.assertEqual(len(result["bufferViews"]), len(owl["bufferViews"]) + 4 + 2)
        self.assertEqual(result["buffers"][0]["byteLength"], len(binary))
        self.assertEqual(merge(*inputs), (result, binary), "Output must be deterministic")

    def test_remaps_nodes_samplers_accessors_and_binary_without_resampling(self):
        owl, owl_binary, source, source_binary = fixtures()
        result, binary = merge(owl, owl_binary, source, source_binary)
        original, copied = source["animations"][1], result["animations"][-1]
        self.assertEqual(copied["extras"], original["extras"])
        self.assertEqual([c["target"]["node"] for c in copied["channels"]], [1, 2, 3])
        self.assertEqual([c["sampler"] for c in copied["channels"]], [1, 0, 2])
        for before, after in zip(original["channels"], copied["channels"]):
            a, b = original["samplers"][before["sampler"]], copied["samplers"][after["sampler"]]
            self.assertEqual(a.get("interpolation"), b.get("interpolation"))
            for field in ("input", "output"):
                self.assertEqual(values(source, source_binary, a[field]), values(result, binary, b[field]))

    def test_bottle_is_hidden_immediately_and_does_not_extend_duration(self):
        result, binary = merge(*fixtures())
        animation = result["animations"][-1]
        hidden = animation["samplers"][animation["channels"][-1]["sampler"]]
        self.assertEqual(values(result, binary, hidden["input"]), (0, 1.5))
        for value in values(result, binary, hidden["output"]):
            self.assertAlmostEqual(value, 0.001)
        self.assertEqual(max(max(values(result, binary, s["input"])) for s in animation["samplers"]), 1.5)

    def test_allows_quaternion_sign_and_rounding_but_rejects_changed_rig(self):
        for change in ("bone", "ancestor", "parent", "matrix"):
            with self.subTest(change=change):
                owl, owl_binary, source, source_binary = fixtures()
                if change == "bone":
                    owl["nodes"][2]["translation"][1] = 1.1
                elif change == "ancestor":
                    owl["nodes"][0]["scale"] = [1, 1, 1]
                elif change == "parent":
                    owl["nodes"][0]["children"].append(2)
                    owl["nodes"][1]["children"].remove(2)
                else:
                    source["nodes"][0]["matrix"] = [1] * 16
                with self.assertRaises(ValueError):
                    merge(owl, owl_binary, source, source_binary)
        owl, owl_binary, source, source_binary = fixtures()
        owl["nodes"][1]["rotation"] = [0, 0, 0, -1.00000001]
        merge(owl, owl_binary, source, source_binary)

    def test_rejects_missing_duplicate_bones_and_clips(self):
        for change in ("missing_bone", "duplicate_bone", "missing_clip", "duplicate_source_clip", "duplicate_target_clip"):
            with self.subTest(change=change):
                owl, owl_binary, source, source_binary = fixtures()
                if change == "missing_bone":
                    owl["nodes"][2]["name"] = "Other"
                elif change == "duplicate_bone":
                    owl["nodes"].append({"name": "Tip"})
                elif change == "missing_clip":
                    source["animations"].pop()
                elif change == "duplicate_source_clip":
                    source["animations"].append(copy.deepcopy(source["animations"][-1]))
                else:
                    owl["animations"].append({"name": DEFAULT_CLIP})
                with self.assertRaises(ValueError):
                    merge(owl, owl_binary, source, source_binary)

    def test_rejects_out_of_range_data_and_invalid_timeline(self):
        owl, owl_binary, source, source_binary = fixtures()
        source["accessors"][1]["count"] = 20
        with self.assertRaisesRegex(ValueError, "buffer range"):
            merge(owl, owl_binary, source, source_binary)
        owl, owl_binary, source, source_binary = fixtures()
        binary = bytearray(source_binary)
        struct.pack_into("<2f", binary, source["bufferViews"][1]["byteOffset"], 1.5, 0)
        with self.assertRaisesRegex(ValueError, "strictly increasing"):
            merge(owl, owl_binary, source, bytes(binary))

    def test_exclusive_output_and_round_trip(self):
        owl, owl_binary, source, source_binary = fixtures()
        with tempfile.TemporaryDirectory() as directory:
            a, b, out = (Path(directory) / name for name in ("owl.glb", "source.glb", "result.glb"))
            a.write_bytes(encode_glb(owl, owl_binary))
            b.write_bytes(encode_glb(source, source_binary))
            for path in (a, b):
                with self.assertRaisesRegex(ValueError, "differ"):
                    create_asset(a, b, path)
            create_asset(a, b, out)
            document, binary = read_glb(out)
            self.assertEqual(document["animations"][-1]["name"], DEFAULT_CLIP)
            self.assertEqual(binary[:len(owl_binary)], owl_binary)
            expected = out.read_bytes()
            with self.assertRaisesRegex(ValueError, "already exists"):
                create_asset(a, b, out)
            self.assertEqual(out.read_bytes(), expected)


if __name__ == "__main__":
    unittest.main()
