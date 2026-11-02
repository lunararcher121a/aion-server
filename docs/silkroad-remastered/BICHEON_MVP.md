# Bicheon Smash A — isolated PvE prototype (Aion 4.8 / Silkroad v1.150)

**Branch:** `silkroad-remastered`; the `4.8` branch is unchanged.

**Scope:** one real server-side Aion `SkillTemplate` (`59001`) with physical
`<skillatk>` damage, a sword gate, MP cost, 4 s experimental cooldown, and
server-authoritative skill training for Silkroad `SKILL_CH_SWORD_SMASH_A_01`
(Silkroad skill id `3`, Bicheon mastery id `257`, mastery >= 5, SP cost `2`).

**This is not yet a client-ready skill.** No animations, hotbar icon, patched
skill data, Chinese mastery UI, or real Silkroad damage coefficients are
included. The Aion 4.8 client does not know template `59001`; testing a cast
may show no proper animation or may cause client compatibility issues.

## Why the implementation is isolated

- Default state is **OFF**. Requires JVM system property:
  `-Dsilkroad.bicheon.enabled=true`.
- Only the optional XML addon `game-server/data/static_data/skills/silkroad_bicheon.xml`
  is registered by `DataManager` when enabled. It has its own
  `skill_data` root and is validated against the original `skills.xsd`.
  The 13 MB original Aion `skill_templates.xml` is **not modified**.
- No Silkroad skill ID is passed as an Aion template ID. Aion uses 59001;
  OpenSRO uses 3. They are deliberately distinct.
- `BicheonRepository` uses a transaction and `SELECT ... FOR UPDATE`
  on `sro_characters` to debit exactly 2 SP and insert learned SRO skill
  rank in the **same transaction**, preventing duplicate purchase.
- The prototype **does not insert 59001 into `player_skills`**. It will
  NOT be forced into the Aion client's normal skill list at login.
- The GM test command directly creates a `Skill` from the verified Aion
  template using `SkillEngine.getSkillFor(player, template, target, 1)`.
  This overload does not require native Aion skill-list membership,
  so `BicheonPrototype.canCast()` and the `Skill.canUseSkill()` check
  explicitly enforce the SRO SQL ownership, mastery >= 5, player level >= 5
  and equipped Aion SWORD.
- `PlayerRestrictions.canUseSkill()` retains original restrictions and adds
  a second SQL-backed gate for this one prototype id. No normal Aion spells
  are affected.
- The normal Aion damage engine, `SkillAttackInstantEffect` and
  `AttackUtil.calculateSkillResult`, compute the physical damage.
  The XML's `value=20` is a **temporary balancing placeholder**, not a
  claim of exact Silkroad 1.150 coefficients.

## TEST ONLY: isolated copy of game-server and a disposable database

1. Make a full game-server database backup. Create a disposable test DB.
2. Apply the **existing** `game-server/sql/silkroad_remastered_v001.sql`
   to the test DB. Do not use production data. This creates only the three
   `sro_*` tables; no original table is changed.
3. Start the game-server with JDK 25 and pass the JVM property
   `-Dsilkroad.bicheon.enabled=true` to the **server JVM**, not to a shell
   that starts a different Java process. Start in the game-server directory
   (so `./data/static_data` paths resolve).
4. Log in with an administrator **level 9** and use a throwaway character at
   least **level 5**. Equip an Aion item of group `SWORD`.
5. In the GM chat, execute in order:

   ```text
   //srobicheon setup
   //srobicheon sp 20
   //srobicheon mastery 5
   //srobicheon status
   //srobicheon learn
   //srobicheon status
   ```

   The first `learn` should charge 2 SP. A repeated `learn` should be
   denied and **must not deduct SP again**. Restart and verify `status`.
6. Target a live hostile NPC from melee distance, then run:

   ```text
   //srobicheon cast
   ```

   Check server logs, NPC HP, MP consumption, range validation, cooldown
   and failed attempts without a sword or valid learned SRO skill.

## Validations not yet executed

- Maven / JUnit suite: **source-level tests authored, not executed here**.
  The repo workflow specifies JDK 25 and uses:
  `mvn -B -T 4 -Dmaven.source.skip=true -Dmaven.test.skip=false test`.
- MariaDB migration and two-transaction race test: **not run**.
- Aion client 4.8 live compatibility: **not run**.
- Native Hotan/Jangan map data, icons and Silkroad animations: **not built**.

## What is needed before exposing to players

1. Build and run JUnit + migration tests on the user's server.
2. Confirm experimental packets don't disconnect the client.
3. Map the skill to an actual client-available ID/motion after client patching
   and a collision audit, and create real icons and an SP/mastery UI.
4. Implement account-wide Chinese character opt-in, real kill/quest SP rewards,
   automatic state load, genuine Silkroad physical coefficients and weapon
   mapping for Sword **and Blade**. This prototype only uses Aion SWORD.
5. Rework the experimental GM command into a secure player-facing learn/cast
   route after compatibility and concurrent-purchase tests pass.

**NEVER enable this feature on a production Aion game-server at this stage.**
