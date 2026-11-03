import tempfile
import unittest
from pathlib import Path
from import_opensro_textdata import extract

class OpenSroImportTests(unittest.TestCase):
    def test_native_monster_and_signed_region(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder)
            fields = ["0"] * 120
            fields[0], fields[1], fields[2], fields[5] = "1", "1933", "MOB_CH_MANGYANG", "SN_MOB_CH_MANGYANG"
            for index, value in {8:1, 9:1, 10:2, 11:1, 52:"monster.bsr", 57:1, 59:100, 77:2.5}.items():
                fields[index] = str(value)
            (path / "characterdata.txt").write_text("\t".join(fields) + "\n", encoding="utf-8")
            (path / "npcpos.txt").write_text("1933\t-32767\t10\t-2\t30\n", encoding="utf-8")
            data = extract(path)
            self.assertEqual(data["monsters"][0]["code"], "MOB_CH_MANGYANG")
            self.assertEqual(data["monsters"][0]["hp"], 100)
            self.assertEqual(data["spawns"][0]["sro_region_id"], 32769)
            self.assertIsNone(data["spawns"][0]["aion_xyz"])
            self.assertFalse(data["runtime_enabled"])

    def test_missing_data_is_error(self):
        with tempfile.TemporaryDirectory() as folder:
            with self.assertRaises(ValueError):
                extract(folder)

if __name__ == "__main__":
    unittest.main()
