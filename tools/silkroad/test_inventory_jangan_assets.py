import tempfile
import unittest
from pathlib import Path
from inventory_jangan_assets import build_inventory

class JanganInventoryTests(unittest.TestCase):
    def test_inventory_is_metadata_only(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "terrain").mkdir()
            (root / "terrain" / "sample.bin").write_bytes(b"test")
            result = build_inventory(root)
            self.assertEqual(result["assets"][0]["category"], "terrain")
            self.assertEqual(result["assets"][0]["size_bytes"], 4)
            self.assertFalse(result["contains_asset_bytes"])
            self.assertFalse(result["aion_client_compatible"])
            self.assertFalse(result["runtime_enabled"])

    def test_missing_root_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            with self.assertRaises(ValueError):
                build_inventory(Path(directory) / "missing")
