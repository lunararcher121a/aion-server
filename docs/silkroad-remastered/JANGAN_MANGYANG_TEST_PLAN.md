# Jangan / Mangyang: evidence-based test plan

## Verified sources

- Aion world IDs are read from `game-server/data/static_data/world_maps.xml`, not guessed.
- OpenSRO code references the Jangan gate as SRO region **25000** in `apps/server/internal/game/action/rebirth.go`. This is **a town gate region, not evidence of Mangyang spawn coordinates**.
- OpenSRO geometry test source `scripts/lib/clipOracleWalkPlane.mjs` refers to a Jangan veranda anchor at region `0x61a7`. This is **not** a Mangyang spawn.
- OpenSRO's retail `npcpos.txt` and `characterdata*.txt` are generated/extracted from a client installation and are not committed in the public repo. Never infer Mangyang's region from the gate region.

## Reproducible offline pipeline

After supplying legitimately obtained OpenSRO v1.150 textdata:

```sh
python tools/silkroad/import_opensro_textdata.py --textdata /path/to/textdata --output build/opensro-world.json
python tools/silkroad/discover_jangan.py --source build/opensro-world.json --output build/mangyang-source-regions.json
python tools/silkroad/extract_aion_worlds.py --output build/aion-worlds.json
```

Inspect `build/mangyang-source-regions.json`. Select an isolated **test** world from `build/aion-worlds.json`, and measure valid Aion XYZ in the running test client. Create a separate verified region-anchor mapping JSON (schema in `AION_REGION_MAPPING.md`). Do not use fictional anchors.

```sh
python tools/silkroad/map_opensro_regions.py --source build/opensro-world.json --mapping verified-regions.json --output build/opensro-aion-mapped.json
python tools/silkroad/plan_mangyang_spawn.py --mapped build/opensro-aion-mapped.json --aion-worlds build/aion-worlds.json --output build/mangyang-test-plan.json
```

The final plan is **disabled and review-only**. It validates Aion world IDs against the real server catalog but cannot verify height, collisions, map geometry or NPC template compatibility. No live spawning or production writes.

**Blockers:** actual extracted `npcpos.txt` + `characterdata*.txt`, measured Aion test location, and confirmed Mangyang-compatible NPC template. Full Jangan 1:1 terrain requires separate geometry/assets pipeline and cannot be achieved by spawn coordinate mapping alone.
