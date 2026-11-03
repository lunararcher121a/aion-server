#!/usr/bin/env python3
"""Offline, read-only Silkroad world data importer. Python 3.10+; no dependencies."""
import argparse
import csv
import json
import math
from pathlib import Path

MONSTER_COLUMNS = {"code", "name", "level", "hp", "attack", "defense"}
SPAWN_COLUMNS = {"code", "region", "x", "y", "z", "respawn_seconds"}
REGION_COLUMNS = {"region", "aion_world_id", "origin_x", "origin_y", "origin_z", "scale", "aion_x", "aion_y", "aion_z"}

def read_rows(path, required):
    with Path(path).open(encoding="utf-8-sig", newline="") as stream:
        reader = csv.DictReader(stream)
        if not reader.fieldnames or not required.issubset(reader.fieldnames):
            raise ValueError(f"{path}: required CSV columns: {', '.join(sorted(required))}")
        return list(reader)

def number(value, name):
    n = float(value)
    if not math.isfinite(n):
        raise ValueError(f"{name} must be finite")
    return n

def positive_int(value, name):
    n = int(value)
    if n <= 0:
        raise ValueError(f"{name} must be positive")
    return n

def import_data(monster_csv, spawn_csv, region_csv):
    monsters = {}
    for row in read_rows(monster_csv, MONSTER_COLUMNS):
        code = row["code"].strip()
        if not code or code in monsters:
            raise ValueError(f"Missing or duplicate monster code: {code}")
        monsters[code] = {
            "code": code, "name": row["name"].strip(),
            "level": positive_int(row["level"], "level"),
            "hp": positive_int(row["hp"], "hp"),
            "attack": positive_int(row["attack"], "attack"),
            "defense": int(row["defense"]),
        }
        if not monsters[code]["name"] or monsters[code]["defense"] < 0:
            raise ValueError(f"Invalid monster: {code}")

    regions = {}
    for row in read_rows(region_csv, REGION_COLUMNS):
        key = row["region"].strip()
        if not key or key in regions:
            raise ValueError(f"Missing or duplicate region: {key}")
        scale = number(row["scale"], "scale")
        if scale <= 0:
            raise ValueError("scale must be positive")
        regions[key] = {
            "world_id": positive_int(row["aion_world_id"], "aion_world_id"),
            "origin": [number(row["origin_" + axis], axis) for axis in "xyz"],
            "aion_origin": [number(row["aion_" + axis], axis) for axis in "xyz"],
            "scale": scale,
        }

    spawns = []
    for row in read_rows(spawn_csv, SPAWN_COLUMNS):
        code, region = row["code"].strip(), row["region"].strip()
        if code not in monsters or region not in regions:
            raise ValueError(f"Unknown monster or unmapped region: {code}/{region}")
        mapping = regions[region]
        source = [number(row[axis], axis) for axis in "xyz"]
        destination = [
            mapping["aion_origin"][i] + (source[i] - mapping["origin"][i]) * mapping["scale"]
            for i in range(3)
        ]
        spawns.append({
            "code": code, "source_region": region, "source_xyz": source,
            "aion_world_id": mapping["world_id"], "aion_xyz": destination,
            "respawn_seconds": positive_int(row["respawn_seconds"], "respawn_seconds"),
            "enabled": False,
        })
    return {"schema_version": 1, "monsters": list(monsters.values()), "regions": regions,
            "spawns": spawns, "runtime_enabled": False}

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--monsters", required=True)
    parser.add_argument("--spawns", required=True)
    parser.add_argument("--regions", required=True)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()
    result = import_data(args.monsters, args.spawns, args.regions)
    output = Path(args.output)
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(result, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"Exported {len(result['monsters'])} monsters, {len(result['spawns'])} disabled spawns to {output}")

if __name__ == "__main__":
    main()
