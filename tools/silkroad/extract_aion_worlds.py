#!/usr/bin/env python3
"""Extract verified Aion world IDs from the server's actual world_maps.xml."""
import argparse
import json
from pathlib import Path
import xml.etree.ElementTree as ET

def extract_worlds(xml_path):
    root = ET.parse(xml_path).getroot()
    if root.tag != "world_maps":
        raise ValueError("Expected Aion world_maps root")
    worlds = []
    seen = set()
    for item in root.findall("map"):
        world_id = int(item.attrib["id"])
        if world_id <= 0 or world_id in seen:
            raise ValueError("Invalid or duplicate Aion world ID")
        seen.add(world_id)
        worlds.append({"world_id": world_id, "name": item.get("name", ""),
                       "code": item.get("cName", ""),
                       "world_size": int(item.get("world_size", "0")),
                       "world_type": item.get("world_type", "")})
    return {"schema_version": 1, "source": str(xml_path), "worlds": worlds,
            "selected_test_world_id": None, "runtime_enabled": False}

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--world-maps", default="game-server/data/static_data/world_maps.xml")
    p.add_argument("--output", required=True)
    args = p.parse_args()
    report = extract_worlds(args.world_maps)
    target = Path(args.output)
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(report, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"Discovered {len(report['worlds'])} real Aion world IDs; no test world selected.")

if __name__ == "__main__":
    main()
