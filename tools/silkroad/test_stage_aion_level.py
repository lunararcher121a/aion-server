import tempfile
import unittest
from pathlib import Path
from stage_aion_level import inventory_level, stage_level

class StageAionLevelTests(unittest.TestCase):
    def test_verified_copy_preserves_original(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d)
            source = root / "source"
            source.mkdir()
            (source / "level.pak").write_bytes(b"example")
            dest = root / "staged"
            manifest = stage_level(source, dest)
            self.assertEqual((dest / "level.pak").read_bytes(), b"example")
            self.assertEqual((source / "level.pak").read_bytes(), b"example")
            self.assertEqual(len(manifest["files"]), 1)
            self.assertFalse(manifest["runtime_tested"])

    def test_reject_nested_destination(self):
        with tempfile.TemporaryDirectory() as d:
            source = Path(d) / "level"
            source.mkdir()
            with self.assertRaises(ValueError):
                stage_level(source, source / "copy")

    def test_inventory_detects_files(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d)
            (root / "terrain.pak").write_bytes(b"abc")
            self.assertEqual(inventory_level(root)["files"][0]["extension"], ".pak")
