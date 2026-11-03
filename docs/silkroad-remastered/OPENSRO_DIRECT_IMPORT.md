# OpenSRO v1.150 direct data importer (offline)

This adapter reads the **actual extracted OpenSRO textdata format** rather than requiring hand-written monster CSVs.

Verified source-code references:
- `opensro-dev/opensro/apps/server/internal/game/world/monster/textdata.go`: characterdata monster type gate (cols 8..11 = 1,1,2,1), RefObjID col 1, code col 2, level col 57, HP col 59, physical defense col 77.
- `opensro-dev/opensro/apps/server/internal/game/world/monster/npcpos.go`: tab-separated `npcpos.txt`, columns refObjID, signed regionID, X, Y, Z. Signed negative region IDs are preserved as uint16 values.
- `textdataname.txt`: localized symbol col 1, English text col 8.

The original extracted client textdata is **not committed** to either repository. You must supply a permitted v1.150 extraction locally; do not publish copyrighted source assets. OpenSRO's AGPL license does not license third-party media.

Run:

```sh
python tools/silkroad/import_opensro_textdata.py --textdata C:/OpenSRO/extracted/media/server_dep/silkroad/textdata --output build/opensro-world.json
python -m unittest discover -s tools/silkroad -p 'test_*.py'
```

Replace the example `--textdata` with the **actual** extracted directory containing `characterdata*.txt`, `npcpos.txt`, optionally `textdataname.txt`.

The importer preserves monster identity, level, HP, physical defense, source model path and region-local spawn coordinates. **It does not invent attack values, map translations, respawn timings, or Aion NPC template IDs**. These are intentionally unset until sourced/calibrated. All monsters/spawns remain disabled, and no GameServer code reads the JSON yet.

The previous CSV importer remains available for manually validated Aion coordinate mappings. A later stage will join source catalogs to target Aion map IDs and safe spawn templates, after an in-game verification.
