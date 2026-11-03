#!/usr/bin/env python3
"""Offline inventory of user-supplied Jangan world exports. No asset redistribution."""
import argparse
import hashlib
import json
from pathlib import Path

KINDS = {"terrain", "buildings", "roads", "collision", "textures", "vegetation", "water"}
def build_inventory(root):
    root = Path(root).resolve()
    if not root.is_dir():
        raise ValueError("Missing local export directory")
    entries = []
    for path in sorted(root.rglob("*")):
        if not path.is_file():
            continue
        rel = path.relative_to(root)
        if len(rel.parts) < 2 or rel.parts[0] not in KINDS:
            continue
        if path.is_symlink() or not path.resolve().is_relative_to(root):
            raise ValueError("Unsafe asset path")
        sha = hashlib.sha256()
        with path.open("rb") as f:
            for block in iter(lambda: f.read(1024 * 1024), b""):
                sha.update(block)
        entries.append({"category": rel.parts[0], "relative_path": rel.as_posix(),
                        "size_bytes": path.stat().st_size, "sha256": sha.hexdigest()})
    present = {entry["category"] for entry in entries}
    return {"schema_version": 1, "project": "Jangan 3D", "source": "user-supplied local exports",
            "runtime_enabled": False, "contains_asset_bytes": False,
            "aion_client_compatible": False, "conversion_verified": False,
            "missing_categories": sorted(KINDS - present), "assets": entries}

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--export-root", required=True)
    p.add_argument("--output", required=True)
    args = p.parse_args()
    result = build_inventory(args.export_root)
    dest = Path(args.output).resolve()
    root = Path(args.export_root).resolve()
    if dest.is_relative_to(root):
        raise ValueError("Write inventory outside asset root")
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
    print(f"Indexed {len(result['assets'])} assets; no assets copied or enabled")

if __name__ == "__main__":
    main()
