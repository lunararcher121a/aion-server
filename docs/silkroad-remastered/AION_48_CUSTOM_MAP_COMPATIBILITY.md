# Aion 4.8 custom-level compatibility investigation — Jangan

## Findings and limitations

Aion 4.8 uses a customized CryEngine-based client. Generic CryEngine documentation describes terrain heightmaps, LevelData, level.pak, and terraintexture.pak; it **does not prove** that an unmodified Aion 4.8 retail client accepts arbitrary new maps.

Evidence:
- https://www.cryengine.com/docs/static/engines/cryengine-3/categories/1638401/pages/1605643
- https://www.cryengine.com/docs/static/engines/cryengine-3/categories/1114113/pages/1048854
- https://github.com/beyond-aion/aion-geobuilder (client terrain, static object, and collision extraction; **not** a custom map writer)
- https://github.com/chengalextw/Aion-geo-viewer (client/level inspection tools; **not** proof of runtime injection)
- https://github.com/beyond-aion/aion-server/tree/4.8 (server-side world support)

## Decision

Do **not** implement a new world ID or patch client binaries yet. Server-side `world_maps.xml` registration is not sufficient to make the client render a new world. A server GeoBuilder output is not a client level archive.

## Minimal compatibility experiment (separate private client and server only)

1. Obtain an authorized Aion 4.8 client installation. Keep a byte-for-byte backup and original file hashes.
2. Identify the existing level directory and inspect it with Monono2 or AionClientViewer. Record exact filenames, structure, compression/encryption, heightmap metadata, and whether the tools support this client build.
3. Determine whether the **same existing level** can load with one reversible, authorized, locally modified static object or texture. If not, stop; do not guess file layouts or patch executables.
4. If the modified level loads, check terrain rendering, object placement, height/physics, zone transitions, minimap, and server-side geodata compatibility.
5. Only after the existing-level experiment works, investigate the additional client registration steps for a separate world and whether this Aion 4.8 build exposes a supported level-authoring path.
6. Only then develop a conversion prototype from permitted Silkroad geometry. Terrain, meshes, material references, navigation/collision, and map metadata are separate workstreams.

## Safety / status

- No client assets available in this repository.
- No verified client level write path or Aion 4.8 runtime loading test.
- No 1:1 Jangan 3D level exists yet.
- No client executable patching, runtime flags, server production edits, or distribution of third-party media.
- All planning remains in the `silkroad-remastered` branch.

## Go / no-go

GO only when a modified existing level renders and collisions can be validated on a private test client. Otherwise use a compatible authoring toolchain or reconsider the rendering engine; do not describe the client as supporting arbitrary new maps without evidence.
