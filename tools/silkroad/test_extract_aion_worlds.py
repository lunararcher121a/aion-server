import tempfile
import unittest
from pathlib import Path
from extract_aion_worlds import extract_worlds

class AionWorldsTests(unittest.TestCase):
    def test_real_xml_schema(self):
        with tempfile.TemporaryDirectory() as d:
            path = Path(d) / "world_maps.xml"
            path.write_text('<world_maps><map id="110010000" cName="LC1" name="Sanctum" world_size="3072" world_type="ELYSEA"/></world_maps>', encoding="utf-8")
            report = extract_worlds(path)
            self.assertEqual(report["worlds"][0]["world_id"], 110010000)
            self.assertIsNone(report["selected_test_world_id"])
            self.assertFalse(report["runtime_enabled"])

    def test_duplicate_ids_fail(self):
        with tempfile.TemporaryDirectory() as d:
            path = Path(d) / "world_maps.xml"
            path.write_text('<world_maps><map id="1"/><map id="1"/></world_maps>', encoding="utf-8")
            with self.assertRaises(ValueError):
                extract_worlds(path)
