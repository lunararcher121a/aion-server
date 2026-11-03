# Mangyang — controlled Aion NPC test

Status: **staged, not executed**. The preset does not auto-spawn.

## Candidate asset
- Candidate native Aion NPC template: `210671` (Kerubar; verify in the actual branch's loaded NPC_DATA and in the exact Aion 4.8 client).
- This is an Aion visual stand-in, not an imported Silkroad model.
- Test values: level 1, HP 100, physical attack 10, physical defense 0. They are **not confirmed OpenSRO stats**.

## Preconditions
1. Compile `game-server` with Java 25 and review errors.
2. Confirm `DataManager.NPC_DATA.getNpcTemplate(210671) != null`; confirm NPC is a hostile, killable creature.
3. Confirm a loaded Aion world ID, reachable safe test coordinates and instance ID.
4. Enable `-Dsilkroad.monsters.enabled=true` only in a disposable test environment.
5. Use `MangyangTestPreset.monster()` and `MangyangTestPreset.spawnAt(worldId,x,y,z)` with `SilkroadMonsterSpawnAdapter.spawn(...,instanceId)` in a controlled invocation (no command/startup integration exists yet).
6. Check HP, attack and defense for that specific NPC and compare an ordinary Kerubar of the same native template.
7. Kill test NPC and observe whether native dynamic spawn reappears. **Respawn is not yet proven.** If it respawns, verify custom stats survive; currently the adapter applies stats only to the first NPC instance, so do not mark Silkroad respawn complete.
8. Check cleanup and ensure no lingering spawns in the test map.

## Remaining work
- Confirm candidate NPC ID and client rendering.
- Add safe GM test command with bounded location and cleanup.
- Implement persistent per-spawn Silkroad identity and per-respawn stat restoration.
- Verify the OpenSRO monster code and authentic stat values from reference data.
- Run GitHub Actions compile and in-game tests.
