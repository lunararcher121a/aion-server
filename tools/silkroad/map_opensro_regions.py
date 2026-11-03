#!/usr/bin/env python3
"""Offline OpenSRO-to-Aion region mapping. Never modifies runtime or spawns."""
import argparse
import json
import math
from pathlib import Path

def finite(value):
    if isinstance(value, bool) or not isinstance(value, (int, float)) or not math.isfinite(value):
        raise ValueError("Expected finite number")
    return float(value)

def map_regions(source, mapping):
    if source.get("schema_version") != 1 or source.get("runtime_enabled") is not False:
        raise ValueError("Source must be a disabled OpenSRO export v1")
    if mapping.get("schema_version") != 1 or not isinstance(mapping.get("regions"), dict):
        raise ValueError("Invalid mapping schema")
    regions = {}
    for key, config in mapping["regions"].items():
        region_id = int(key)
        if str(region_id) != key or not 0 <= region_id <= 65535:
            raise ValueError("Invalid SRO region ID")
        if config.get("verified") is not True:
            raise ValueError("Unverified mapping: " + key)
        world = config.get("aion_world_id")
        if isinstance(world, bool) or not isinstance(world, int) or world <= 0:
            raise ValueError("Invalid Aion world ID")
        a, b = config.get("sro_anchor_xyz"), config.get("aion_anchor_xyz")
        if not isinstance(a, list) or not isinstance(b, list) or len(a) != 3 or len(b) != 3:
            raise ValueError("Missing 3D anchors")
        scale = finite(config.get("scale"))
        if scale <= 0:
            raise ValueError("Scale must be positive")
        regions[region_id] = (world, list(map(finite, a)), list(map(finite, b)), scale)
    output, missing = [], set()
    for row in source.get("spawns", []):
        region = row["sro_region_id"]
        if isinstance(region, bool) or not isinstance(region, int) or not 0 <= region <= 65535:
            raise ValueError("Invalid source region")
        xyz = row["sro_xyz"]
        if not isinstance(xyz, list) or len(xyz) != 3:
            raise ValueError("Invalid source position")
        xyz = list(map(finite, xyz))
        spawn = dict(row, enabled=False, aion_world_id=None, aion_xyz=None)
        if region in regions:
            world, src, dst, scale = regions[region]
            spawn["aion_world_id"] = world
            spawn["aion_xyz"] = [dst[i] + (xyz[i] - src[i]) * scale for i in range(3)]
        else:
            missing.add(region)
        output.append(spawn)
    return {"schema_version": 1, "source": "OpenSRO verified Aion mapping",
            "runtime_enabled": False, "requires_in_game_validation": True,
            "monsters": source.get("monsters", []), "spawns": output,
            "mapped_spawn_count": sum(s["aion_world_id"] is not None for s in output),
            "unmapped_region_ids": sorted(missing)}

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--source", required=True)
    p.add_argument("--mapping", required=True)
    p.add_argument("--output", required=True)
    args = p.parse_args()
    result = map_regions(json.loads(Path(args.source).read_text(encoding="utf-8")),
                         json.loads(Path(args.mapping).read_text(encoding="utf-8")))
    path = Path(args.output)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Mapped {result['mapped_spawn_count']}/{len(result['spawns'])} disabled spawns")

if __name__ == "__main__":
    main()
