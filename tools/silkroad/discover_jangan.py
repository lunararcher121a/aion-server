#!/usr/bin/env python3
"""Read-only discovery of real Mangyang spawn regions from an OpenSRO export."""
import argparse
import json
from collections import Counter
from pathlib import Path

def build_report(data, name="MANGYANG"):
    if data.get("schema_version") != 1 or data.get("runtime_enabled") is not False:
        raise ValueError("Expected disabled OpenSRO source export v1")
    monsters = [m for m in data.get("monsters", []) if name.upper() in m.get("code", "").upper()]
    codes = {m["code"] for m in monsters}
    spawns = [s for s in data.get("spawns", []) if s.get("code") in codes]
    regions = Counter(s["sro_region_id"] for s in spawns)
    return {
        "search": name, "source": data.get("source"), "monster_count": len(monsters),
        "spawn_anchor_count": len(spawns),
        "monsters": monsters,
        "regions": [{"sro_region_id": rid, "anchor_count": count,
                     "sample_sro_xyz": next(s["sro_xyz"] for s in spawns if s["sro_region_id"] == rid)}
                    for rid, count in sorted(regions.items())],
        "requires_verified_aion_world_id": True, "runtime_enabled": False
    }

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--source", required=True)
    p.add_argument("--output", required=True)
    p.add_argument("--search", default="MANGYANG")
    args = p.parse_args()
    data = json.loads(Path(args.source).read_text(encoding="utf-8"))
    report = build_report(data, args.search)
    target = Path(args.output)
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(report, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"Found {report['monster_count']} monster variants, {report['spawn_anchor_count']} anchors in {len(report['regions'])} source regions.")

if __name__ == "__main__":
    main()
