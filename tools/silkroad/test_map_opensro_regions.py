import unittest
from map_opensro_regions import map_regions

class RegionMappingTests(unittest.TestCase):
    def setUp(self):
        self.source = {"schema_version": 1, "runtime_enabled": False, "monsters": [],
                       "spawns": [{"code": "MOB_CH_MANGYANG", "sro_region_id": 258,
                                   "sro_xyz": [12, 24, -2], "enabled": False},
                                  {"code": "OTHER", "sro_region_id": 259,
                                   "sro_xyz": [0, 0, 0], "enabled": False}]}
        self.mapping = {"schema_version": 1, "regions": {"258": {
            "verified": True, "aion_world_id": 210010000,
            "sro_anchor_xyz": [10, 20, 0], "aion_anchor_xyz": [100, 200, 300], "scale": 2}}}

    def test_transform_and_disabled(self):
        result = map_regions(self.source, self.mapping)
        self.assertEqual(result["mapped_spawn_count"], 1)
        self.assertEqual(result["spawns"][0]["aion_xyz"], [104, 208, 296])
        self.assertIsNone(result["spawns"][1]["aion_xyz"])
        self.assertEqual(result["unmapped_region_ids"], [259])
        self.assertTrue(all(not s["enabled"] for s in result["spawns"]))
        self.assertFalse(result["runtime_enabled"])

    def test_reject_unverified(self):
        self.mapping["regions"]["258"]["verified"] = False
        with self.assertRaises(ValueError):
            map_regions(self.source, self.mapping)

    def test_reject_invalid_world(self):
        self.mapping["regions"]["258"]["aion_world_id"] = 0
        with self.assertRaises(ValueError):
            map_regions(self.source, self.mapping)

    def test_reject_enabled_source(self):
        self.source["runtime_enabled"] = True
        with self.assertRaises(ValueError):
            map_regions(self.source, self.mapping)

if __name__ == "__main__":
    unittest.main()
