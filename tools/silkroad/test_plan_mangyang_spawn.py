import unittest
from plan_mangyang_spawn import build_plan

class SpawnPlanTests(unittest.TestCase):
    def test_only_known_worlds_and_never_enabled(self):
        mapped = {"runtime_enabled": False, "requires_in_game_validation": True,
                  "spawns": [{"code": "MOB_CH_MANGYANG", "sro_region_id": 25000,
                              "sro_xyz": [1, 2, 3], "aion_world_id": 110010000,
                              "aion_xyz": [10, 20, 30], "enabled": False},
                             {"code": "MOB_CH_MANGYANG", "sro_region_id": 25001,
                              "sro_xyz": [1, 2, 3], "aion_world_id": 999,
                              "aion_xyz": [10, 20, 30], "enabled": False}]}
        catalog = {"runtime_enabled": False, "worlds": [{"world_id": 110010000, "name": "Sanctum"}]}
        result = build_plan(mapped, catalog)
        self.assertEqual(result["candidate_count"], 1)
        self.assertFalse(result["ready_to_spawn"])
        self.assertFalse(result["candidates"][0]["enabled"])

    def test_reject_runtime_export(self):
        with self.assertRaises(ValueError):
            build_plan({"runtime_enabled": True}, {"runtime_enabled": False, "worlds": []})
