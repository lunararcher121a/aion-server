import unittest
from discover_jangan import build_report

class JanganDiscoveryTests(unittest.TestCase):
    def test_actual_regions_come_from_input(self):
        source = {"schema_version": 1, "runtime_enabled": False, "source": "fixture",
                  "monsters": [{"code": "MOB_CH_MANGYANG", "hp": 100}],
                  "spawns": [{"code": "MOB_CH_MANGYANG", "sro_region_id": 4660,
                              "sro_xyz": [12, 5, 9]}]}
        report = build_report(source)
        self.assertEqual(report["regions"][0]["sro_region_id"], 4660)
        self.assertEqual(report["spawn_anchor_count"], 1)
        self.assertFalse(report["runtime_enabled"])

    def test_reject_enabled_input(self):
        with self.assertRaises(ValueError):
            build_report({"schema_version": 1, "runtime_enabled": True})
