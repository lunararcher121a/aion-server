# Mangyang GM command integration status

Implemented:
- `SroMangyangCommand` (extends `AdminCommand`), intended syntax `//sromangyang spawn`.
- Native spawn bridge and per-spawn registry.
- `SpawnEngine.spawnObject` reapplies Silkroad stats when the same `SpawnTemplate` is reused by `RespawnService`.
- Test feature flag `-Dsilkroad.monsters.enabled=true` (default off).

**GM registration now completed:**
- `data/handlers/admincommands/SroMangyang.java` is in the dynamically loaded handler directory.
- `config/administration/commands.properties` grants `sromangyang = 9`.
- `//sromangyang check` checks if candidate NPC 210671 exists in the loaded NPC data (does not spawn).
- `//sromangyang spawn` is gated by `-Dsilkroad.monsters.enabled=true`.

**Not yet integrated / verified:**
- Confirm candidate NPC ID 210671 exists in the actual Aion 4.8 NPC dataset and client.
- Build/compile and actual in-game test.
- Test-safe spawn placement (GM position may be blocked or unsafe), manual cleanup and limits.
- Lifetime cleanup of spawn registry entries when the test is retired. Weak references avoid permanently retaining templates, but an active respawn may still hold the template.
- Verify whether respawn's native AI allows respawn, and whether its new NPC receives custom HP before the client first sees it. The registry currently applies after native `spawnObject` creates the NPC.
- Aion NPC level, name, AI and animations still come from the mapped Aion template; Silkroad overrides currently cover HP, physical attack and physical defense only.

Do not enable this on production. First compile and wire command registration, then test on a disposable world.

CI inspection: commit status returned no statuses, and PR workflow lookup returned no runs; neither proves a successful build. Run the push-triggered Actions workflow or compile on a test machine before enabling spawns.
