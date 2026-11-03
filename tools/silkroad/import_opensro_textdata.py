#!/usr/bin/env python3
"""Offline import of *actual* OpenSRO v1.150 extracted textdata; no network, no runtime writes."""
import argparse
import csv
import glob
import json
from pathlib import Path

def rows(path):
    with Path(path).open(encoding="utf-8-sig", errors="replace", newline="") as f:
        for row in csv.reader(f, delimiter="\t"):
            if row and not row[0].startswith("//"):
                yield row

def extract(directory):
    directory = Path(directory)
    if not list(directory.glob("characterdata*.txt")):
        raise ValueError("No characterdata*.txt files; supply extracted OpenSRO v1.150 textdata")
    names = {}
    if (directory / "textdataname.txt").is_file():
        for r in rows(directory / "textdataname.txt"):
            if len(r) > 8 and r[1].startswith("SN_") and r[8].strip():
                names[r[1]] = r[8].strip()
    monsters_by_id = {}
    for path in sorted(directory.glob("characterdata*.txt")):
        for r in rows(path):
            if len(r) <= 77 or r[0].strip() != "1":
                continue
            try:
                if tuple(int(r[i]) for i in (8, 9, 10, 11)) != (1, 1, 2, 1):
                    continue
                ref_id = int(r[1])
                level, hp = int(r[57]), int(r[59])
                defense = float(r[77])
                if ref_id <= 0 or level <= 0 or hp <= 0 or defense < 0:
                    continue
            except ValueError:
                continue
            if ref_id in monsters_by_id:
                raise ValueError(f"Duplicate RefObjID: {ref_id}")
            monsters_by_id[ref_id] = {
                "ref_obj_id": ref_id, "code": r[2].strip(),
                "name": names.get(r[5], r[2].strip()), "level": level,
                "hp": hp, "physical_defense": defense,
                "source_model_path": r[52] if len(r) > 52 else "",
                "aion_npc_id": None, "enabled": False,
            }
    spawns = []
    npcpos = directory / "npcpos.txt"
    if not npcpos.is_file():
        raise ValueError("Missing npcpos.txt")
    for r in rows(npcpos):
        if len(r) < 5:
            continue
        try:
            ref_id = int(r[0])
            region_signed = int(r[1])
            if not -32768 <= region_signed <= 65535:
                continue
            coords = [float(r[i]) for i in (2, 3, 4)]
            if not all(__import__("math").isfinite(x) for x in coords):
                continue
        except ValueError:
            continue
        if ref_id in monsters_by_id:
            spawns.append({
                "ref_obj_id": ref_id, "code": monsters_by_id[ref_id]["code"],
                "sro_region_id": region_signed & 0xffff,
                "sro_xyz": coords, "aion_world_id": None,
                "aion_xyz": None, "enabled": False,
            })
    return {
        "schema_version": 1, "source": "OpenSRO v1.150 extracted textdata",
        "runtime_enabled": False, "requires_aion_mapping": True,
        "monsters": list(monsters_by_id.values()), "spawns": spawns,
    }

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--textdata", required=True, help="Directory containing characterdata*.txt and npcpos.txt")
    p.add_argument("--output", required=True)
    args = p.parse_args()
    data = extract(args.textdata)
    out = Path(args.output)
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Imported {len(data['monsters'])} monster references and {len(data['spawns'])} spawn anchors; all disabled.")

if __name__ == "__main__":
    main()
