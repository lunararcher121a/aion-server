#!/usr/bin/env python3
"""Generate a disabled, review-only Mangyang spawn candidate from verified exports."""
import argparse
import json
import math
from pathlib import Path

def build_plan(mapped, catalog):
    if mapped.get("runtime_enabled") is not False or mapped.get("requires_in_game_validation") is not True:
        raise ValueError("Expected disabled mapped export requiring game validation")
    if catalog.get("runtime_enabled") is not False:
        raise ValueError("Expected offline Aion world catalog")
    worlds = {w["world_id"]: w for w in catalog.get("worlds", [])}
    matches = [s for s in mapped.get("spawns", []) if "MANGYANG" in s.get("code", "").upper()
               and s.get("aion_world_id") in worlds and s.get("aion_xyz") is not None]
    candidates = []
    for s in matches:
        xyz = s["aion_xyz"]
        if len(xyz) != 3 or not all(isinstance(v, (float, int)) and not isinstance(v, bool) and math.isfinite(v) for v in xyz):
            raise ValueError("Invalid mapped coordinates")
        candidates.append({"monster_code": s["code"], "sro_region_id": s["sro_region_id"],
                           "sro_xyz": s["sro_xyz"], "aion_world_id": s["aion_world_id"],
                           "aion_world_name": worlds[s["aion_world_id"]]["name"],
                           "aion_xyz_unverified": xyz, "enabled": False})
    return {"schema_version": 1, "plan_type": "mangyang_manual_test_review",
            "runtime_enabled": False, "spawn_enabled": False,
            "requires_in_game_terrain_validation": True,
            "requires_verified_aion_npc_template": True,
            "candidates": candidates, "candidate_count": len(candidates),
            "ready_to_spawn": False}

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--mapped", required=True)
    p.add_argument("--aion-worlds", required=True)
    p.add_argument("--output", required=True)
    args = p.parse_args()
    plan = build_plan(json.loads(Path(args.mapped).read_text(encoding="utf-8")),
                      json.loads(Path(args.aion_worlds).read_text(encoding="utf-8")))
    out = Path(args.output)
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(plan, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"Prepared {plan['candidate_count']} disabled Mangyang test candidates; none are ready to spawn.")

if __name__ == "__main__":
    main()
