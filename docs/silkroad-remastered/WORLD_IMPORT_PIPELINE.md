# Offline Silkroad monster + world mapping pipeline

This is **stage 1**, not a 3D map or client-asset converter. No copyrighted client assets are included. The importer reads **user-supplied, legally usable CSV exports**; it does not scrape or extract proprietary client archives.

## Input formats

- `monsters.csv`: `code,name,level,hp,attack,defense`
- `spawns.csv`: `code,region,x,y,z,respawn_seconds`
- `regions.csv`: `region,aion_world_id,origin_x,origin_y,origin_z,scale,aion_x,aion_y,aion_z`

`regions.csv` must contain **verified** target Aion map IDs and manually calibrated anchor points. For now, the coordinate mapping is an **axis-aligned uniform scale and translation**. It does **not** account for different terrain, rotated zones, roads, height maps or client 3D geometry. Do not treat mapped coordinates as playable until checked in game.

Example command (Python 3.10+):

```sh
python tools/silkroad/import_world.py --monsters monsters.csv --spawns spawns.csv --regions regions.csv --output build/silkroad-world.json
python -m unittest discover -s tools/silkroad -p 'test_*.py'
```

Output is JSON with `runtime_enabled: false` and each spawn `enabled: false`. It is **not** automatically loaded by GameServer, does not create NPCs, and does not change production. No hardcoded fake Silkroad coordinates or unverified Aion world IDs are shipped.

Next steps: inspect OpenSRO's actual source tables/export formats, write an explicit field adapter, verify map anchors and NPC template IDs, then add safe server-side import with feature flags. Original Silkroad models, textures, maps and animations require separate rights and client-format compatibility review.
