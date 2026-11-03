import csv
import tempfile
import unittest
from pathlib import Path
from import_world import import_data

class WorldImportTests(unittest.TestCase):
    def write_csv(self, directory, name, header, rows):
        path = Path(directory) / name
        with path.open("w", newline="", encoding="utf-8") as file:
            writer = csv.writer(file)
            writer.writerow(header)
            writer.writerows(rows)
        return path

    def test_transform_and_disabled_output(self):
        with tempfile.TemporaryDirectory() as d:
            monsters = self.write_csv(d, "monsters.csv",
                ["code", "name", "level", "hp", "attack", "defense"],
                [["MOB_CH_MANGYANG", "Mangyang", 1, 100, 10, 0]])
            regions = self.write_csv(d, "regions.csv",
                ["region", "aion_world_id", "origin_x", "origin_y", "origin_z",
                 "scale", "aion_x", "aion_y", "aion_z"],
                [["TEST_REGION", 1, 10, 20, 30, 2, 100, 200, 300]])
            spawns = self.write_csv(d, "spawns.csv",
                ["code", "region", "x", "y", "z", "respawn_seconds"],
                [["MOB_CH_MANGYANG", "TEST_REGION", 12, 23, 34, 60]])
            result = import_data(monsters, spawns, regions)
            self.assertEqual(result["spawns"][0]["aion_xyz"], [104, 206, 308])
            self.assertFalse(result["spawns"][0]["enabled"])
            self.assertFalse(result["runtime_enabled"])

    def test_reject_unknown_region(self):
        with tempfile.TemporaryDirectory() as d:
            monsters = self.write_csv(d, "monsters.csv",
                ["code", "name", "level", "hp", "attack", "defense"],
                [["MOB_CH_MANGYANG", "Mangyang", 1, 100, 10, 0]])
            regions = self.write_csv(d, "regions.csv",
                ["region", "aion_world_id", "origin_x", "origin_y", "origin_z",
                 "scale", "aion_x", "aion_y", "aion_z"], [])
            spawns = self.write_csv(d, "spawns.csv",
                ["code", "region", "x", "y", "z", "respawn_seconds"],
                [["MOB_CH_MANGYANG", "JANGAN", 1, 2, 3, 60]])
            with self.assertRaises(ValueError):
                import_data(monsters, spawns, regions)

if __name__ == "__main__":
    unittest.main()
