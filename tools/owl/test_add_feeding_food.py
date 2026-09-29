"""Contract tests for GLB reference remapping and preserved owl animation data."""

import struct
import tempfile
import unittest
from pathlib import Path

from add_feeding_food import (
    EATING_NAME, EATING_SOURCE, FINAL_SCALE_FACTOR, FOOD_NODES, HAND_TIPS, LEFT_ROTATION, RIGHT_ROTATION,
    create_asset, encode_glb, merge, read_glb,
)


def fixtures():
    owl_binary = struct.pack("<2f", 0.0, 10.0) + b"OWL ORIGINAL BIN"
    owl = {
        "asset": {"version": "2.0"}, "buffers": [{"byteLength": len(owl_binary)}],
        "bufferViews": [{"buffer": 0, "byteOffset": 0, "byteLength": 8}],
        "accessors": [{"bufferView": 0, "componentType": 5126, "count": 2, "type": "SCALAR"}],
        "nodes": [{"name": HAND_TIPS[0], "children": [2]}, {"name": HAND_TIPS[1]},
                  {"name": "Accessory_medal", "mesh": 0}],
        "meshes": [{"name": "OriginalMesh", "primitives": []}],
        "images": [{"bufferView": 0, "mimeType": "image/png"}],
        "textures": [{"source": 0, "sampler": 0}], "samplers": [{"wrapS": 33071}],
        "materials": [{"name": "BakedMaterial"}],
        "animations": [{"name": name, "samplers": [{"input": 0, "output": 0}],
                        "channels": [{"sampler": 0, "target": {"node": 0, "path": "rotation"}}]}
                       for name in (EATING_SOURCE, "Idle_3")],
        "scenes": [{"nodes": [0, 1]}], "scene": 0,
    }
    food_binary = bytes(range(96))
    food = {
        "asset": {"version": "2.0"}, "buffers": [{"byteLength": len(food_binary)}],
        "bufferViews": [{"buffer": 0, "byteOffset": i * 32, "byteLength": 32} for i in range(3)],
        "accessors": [{"bufferView": 0, "componentType": 5126, "count": 2, "type": "VEC3"},
                      {"bufferView": 1, "componentType": 5123, "count": 3, "type": "SCALAR"},
                      {"componentType": 5126, "count": 2, "type": "VEC3", "sparse": {
                          "count": 1, "indices": {"bufferView": 1, "componentType": 5123},
                          "values": {"bufferView": 0}}}],
        "images": [{"bufferView": 2, "mimeType": "image/jpeg"}],
        "samplers": [{"minFilter": 9987}], "textures": [{"source": 0, "sampler": 0}],
        "materials": [{"name": "BakedMaterial", "normalTexture": {"index": 0},
                       "occlusionTexture": {"index": 0}, "emissiveTexture": {"index": 0},
                       "pbrMetallicRoughness": {"baseColorTexture": {"index": 0},
                                                "metallicRoughnessTexture": {"index": 0}}}],
        "meshes": [{"primitives": [{"attributes": {"POSITION": 0}, "indices": 1,
                                     "material": 0, "targets": [{"POSITION": 2}]}]}],
        "nodes": [{"name": "SourceFood", "mesh": 0, "translation": [2, 3, 4],
                   "rotation": [0, 0, 0, 1], "scale": [2, 1, 0.5]}],
        "scenes": [{"nodes": [0]}], "scene": 0,
    }
    return owl, owl_binary, food, food_binary


def values(document, binary, accessor_index, width):
    accessor = document["accessors"][accessor_index]
    view = document["bufferViews"][accessor["bufferView"]]
    return struct.unpack_from("<" + "f" * accessor["count"] * width, binary, view["byteOffset"])


class MergeFoodTests(unittest.TestCase):
    def setUp(self):
        self.inputs = fixtures()
        self.document, self.binary = merge(*self.inputs)

    def test_original_binary_channels_and_accessories_are_preserved(self):
        owl, original_binary, _, food_binary = self.inputs
        self.assertEqual(self.binary[:len(original_binary)], original_binary)
        self.assertEqual(self.binary[len(original_binary):len(original_binary) + len(food_binary)], food_binary)
        self.assertEqual(self.document["nodes"][2], owl["nodes"][2])
        self.assertEqual(self.document["animations"][1], owl["animations"][1])
        self.assertEqual(self.document["animations"][0]["channels"][:1], owl["animations"][0]["channels"])
        self.assertEqual(self.document["animations"][0]["samplers"][:1], owl["animations"][0]["samplers"])
        for key in ("bufferViews", "accessors", "images", "textures", "samplers", "materials", "meshes"):
            self.assertEqual(self.document[key][:len(owl[key])], owl[key])
        self.assertEqual(self.inputs, fixtures(), "merge must not mutate either source document")

    def test_each_tip_owns_food_and_both_renderables_share_one_mesh(self):
        for index, name in enumerate(FOOD_NODES):
            anchor = self.document["nodes"][self.document["nodes"][index]["children"][-1]]
            self.assertEqual(anchor["name"], name + "_Attachment")
            self.assertEqual(anchor["rotation"], list((LEFT_ROTATION, RIGHT_ROTATION)[index]))
            rendered = self.document["nodes"][anchor["children"][0]]
            self.assertEqual(rendered["name"], name)
            self.assertEqual(rendered["mesh"], 1)
            for key in ("translation", "rotation", "scale"):
                self.assertEqual(rendered[key], self.inputs[2]["nodes"][0][key])
        self.assertEqual(len(self.document["meshes"]), 2)
        self.assertEqual(self.document["nodes"][0]["children"][0], 2)

    def test_source_matrix_is_preserved_without_competing_trs(self):
        owl, owl_binary, food, food_binary = fixtures()
        matrix = [1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 7, 8, 9, 1]
        food["nodes"][0] = {"mesh": 0, "matrix": matrix}
        result, _ = merge(owl, owl_binary, food, food_binary, translation=(1, 2, 3))
        rendered = next(node for node in result["nodes"] if node.get("name") == "Food_Left")
        self.assertEqual(rendered["matrix"], matrix)
        self.assertFalse(set(rendered) & {"translation", "rotation", "scale"})

    def test_all_imported_references_and_binary_offsets_are_remapped(self):
        d = self.document
        offset = len(self.inputs[1])
        for i, view in enumerate(d["bufferViews"][1:4]):
            self.assertEqual(view["byteOffset"], offset + i * 32)
        self.assertEqual(d["accessors"][1]["bufferView"], 1)
        self.assertEqual(d["accessors"][2]["bufferView"], 2)
        sparse = d["accessors"][3]["sparse"]
        self.assertEqual(sparse["indices"]["bufferView"], 2)
        self.assertEqual(sparse["values"]["bufferView"], 1)
        self.assertEqual(d["images"][1]["bufferView"], 3)
        self.assertEqual(d["textures"][1], {"source": 1, "sampler": 1})
        material = d["materials"][1]
        self.assertEqual(material["name"], "Food_BakedMaterial")
        for field in ("normalTexture", "occlusionTexture", "emissiveTexture"):
            self.assertEqual(material[field]["index"], 1)
        for field in ("baseColorTexture", "metallicRoughnessTexture"):
            self.assertEqual(material["pbrMetallicRoughness"][field]["index"], 1)
        primitive = d["meshes"][1]["primitives"][0]
        self.assertEqual(primitive, {"attributes": {"POSITION": 1}, "indices": 2,
                                     "material": 1, "targets": [{"POSITION": 3}]})

    def test_eating_shrink_covers_clip_and_keeps_final_transform_invertible(self):
        animation = self.document["animations"][0]
        self.assertEqual(animation["name"], EATING_NAME)
        sampler = animation["samplers"][-1]
        times = values(self.document, self.binary, sampler["input"], 1)
        for actual, expected in zip(times, (0, 1.8, 9, 10)):
            self.assertAlmostEqual(actual, expected)
        scales = values(self.document, self.binary, sampler["output"], 3)
        for x in scales[:6]:
            self.assertAlmostEqual(x, 0.16)
        for x in scales[6:]:
            self.assertAlmostEqual(x, 0.16 * FINAL_SCALE_FACTOR)
            self.assertGreater(x, 0)
        self.assertEqual(sampler["interpolation"], "LINEAR")
        self.assertEqual(len(animation["channels"]), 3)
        for channel in animation["channels"][-2:]:
            self.assertEqual(channel["sampler"], 1)
            self.assertEqual(channel["target"]["path"], "scale")
            self.assertTrue(self.document["nodes"][channel["target"]["node"]]["name"].endswith("_Attachment"))

    def test_duplicate_missing_bone_and_unsupported_extensions_fail_clearly(self):
        with self.assertRaisesRegex(ValueError, "already contains"):
            merge(self.document, self.binary, self.inputs[2], self.inputs[3])
        owl, owl_binary, food, food_binary = fixtures()
        owl["nodes"][1]["name"] = "WrongBone"
        with self.assertRaisesRegex(ValueError, "RightHandMiddle4"):
            merge(owl, owl_binary, food, food_binary)
        owl, owl_binary, food, food_binary = fixtures()
        food["meshes"][0]["primitives"][0]["extensions"] = {"KHR_draco_mesh_compression": {}}
        with self.assertRaisesRegex(ValueError, "Unsupported food glTF extensions"):
            merge(owl, owl_binary, food, food_binary)

    def test_file_output_is_deterministic_and_never_overwrites_inputs(self):
        with tempfile.TemporaryDirectory() as directory:
            owl_path, food_path, output_path = (Path(directory) / name for name in ("owl.glb", "food.glb", "merged.glb"))
            owl, owl_binary, food, food_binary = self.inputs
            owl_bytes = encode_glb(owl, owl_binary)
            owl_path.write_bytes(owl_bytes)
            food_path.write_bytes(encode_glb(food, food_binary))
            with self.assertRaisesRegex(ValueError, "differ"):
                create_asset(owl_path, food_path, owl_path)
            with self.assertRaisesRegex(ValueError, "differ"):
                create_asset(owl_path, food_path, food_path)
            create_asset(owl_path, food_path, output_path)
            self.assertEqual(output_path.read_bytes(), encode_glb(self.document, self.binary))
            self.assertEqual(read_glb(output_path), (self.document, self.binary))
            self.assertEqual(owl_path.read_bytes(), owl_bytes)
            with self.assertRaisesRegex(ValueError, "already exists"):
                create_asset(owl_path, food_path, output_path)


if __name__ == "__main__":
    unittest.main()
