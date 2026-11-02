# Silkroad skill ownership integration contract

## Single source of truth
- `sro_characters.skill_points`, `sro_masteries`, and `sro_learned_skills` are authoritative for Silkroad progression.
- Native `player_skills` is **not** written by the Silkroad learning transaction. This avoids duplicate persistence and mismatched schema assumptions.
- The Aion `PlayerSkillList` is a runtime projection for the Aion SkillEngine, not a second source of ownership.

## Bicheon Smash A
- OpenSRO skill ID 3 maps explicitly to Aion template ID 59001; never interpret Silkroad ID 3 as Aion ID 3.
- A single SQL transaction locks `sro_characters`, checks mastery and existing learned rank, debits 2 SP, inserts `sro_learned_skills`, and commits. A repeated request cannot charge SP twice.
- On login, **after native skills load and before skill-list packets are sent**, project only verified Silkroad learned skills into `PlayerSkillList` as non-persistent runtime entries. Never use normal `addSkill` here because it marks entries NEW and could insert into `player_skills` on logout.
- Immediately after learning, apply the same runtime projection and notify the client; on logout discard the projection without saving to `player_skills`.
- If the Silkroad database is unavailable, fail closed: do not expose or cast Bicheon.
- The server must re-check persisted learned rank, mastery, enabled flag, weapon and native combat restrictions on each cast. The client packet never supplies trusted authorization.
- Do not enable client casting until the Aion 4.8 client recognizes template 59001, with compatible animation and icon. A server-only GM harness is not evidence of client compatibility.

## Tests required before enabling
1. SQL migration on disposable DB; verify the real native `player_skills` schema is unchanged.
2. Concurrent learn requests: exactly one successful 2-SP debit and one learned row.
3. Failed insert: SP debit rolled back.
4. Login/logout/relogin: skill projected once, and no native `player_skills` row created.
5. Unlearned skill, insufficient mastery, wrong weapon, cooldown and invalid target rejected at packet and SkillEngine layers.
6. Client 4.8 packet, animation and physical damage tested against a monster.

## Current status
The GM prototype and server-side authorization exist, but native login projection, packet-visible skill, real client assets, compilation and runtime tests are not yet verified.
