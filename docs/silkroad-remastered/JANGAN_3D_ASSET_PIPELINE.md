# Jangan 3D — asset inventory and integration gates

## Purpose
Recreate the recognizable Jangan world layout in Aion 4.8 without confusing coordinate conversion with client map compatibility.

OpenSRO's asset pipeline reads a **user-supplied permitted** Silkroad v1.150 installation. Original media is not committed to OpenSRO or this repository. Do not commit or distribute third-party geometry, textures, animations, or extracted game media without permission.

## Local staging layout

Place permitted exports in a local folder, classified as:

- `terrain/` — ground height and geometry
- `buildings/` — structures, walls and gates
- `roads/` — road placement and geometry
- `collision/` — walkability and navigation
- `textures/` — materials
- `vegetation/` — plants and props
- `water/` — water geometry

These are **organizational folders**, not a claim that OpenSRO emits these paths or that Aion supports these formats.

```sh
python tools/silkroad/inventory_jangan_assets.py --export-root /path/to/your/permitted/jangan-export --output build/jangan-asset-inventory.json
```

This produces hashes, paths, file sizes and missing-category warnings only; **no media copied**, no proprietary assets added to GitHub.

## Technical gates

1. Obtain authorized client v1.150 media and run OpenSRO's documented `pnpm assets prepare` and `pnpm assets build full` locally (see upstream docs).
2. Inspect actual generated world file formats and geometry coordinates. Record provenance and license.
3. Confirm whether Aion 4.8 client has a supported/customizable terrain and object loading path; XML world ID alone is insufficient.
4. Build a tiny offline terrain-and-collision proof of concept, then validate height, movement and visual rendering on a private test client.
5. Only after these checks implement a Jangan zone conversion, keeping all runtime flags off.

The inventory tool does not generate meshes, client-compatible maps, buildings or navmesh. Production and default Aion branch must remain untouched.
