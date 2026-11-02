# Chinese Mastery System — audit and implementation status

Base branch: `4.8`. Work branch: `silkroad-remastered`.

## Verified existing behavior
- `PlayerCommonData` holds `exp`, derives level using Aion's experience table, and sends Aion EXP packets.
- `PlayerGameStats.updateStatsTemplate()` delegates base stats to `PlayerClass.createStatsTemplate(level)`. Attack speed and range depend on equipment.
- `SkillLearnService.learnNewSkills()` auto-learns class/race/level skills from `SKILL_TREE_DATA`; `onLearnSkill` sends `SM_SKILL_LIST` and applies passives.
- `PlayerController.onLevelChange()` invokes Aion auto-learning. `useSkill()` validates via `PlayerRestrictions.canUseSkill`.
- `PlayerDAO` persists `players` including `player_class`, `exp`, `world_id`, position. The SQL `players` table has integer primary key `id`, enum class/race, and EXP bigint.
- `player_skills` uses composite primary key `(player_id, skill_id)` with FK to `players`.
- Network packet implementations include `SM_SKILL_LIST`, `SM_SKILL_REMOVE`, `SM_STATS_INFO`, `SM_STATUPDATE_EXP`; client handlers include `CM_CREATE_CHARACTER`, `CM_LEVEL_READY`, and skill-use packet handlers.

## Added on this branch
- Isolated, server-authoritative Chinese mastery enum and progression rules.
- Additive SQL schema for Chinese character attributes, mastery levels and skill ownership.
- JUnit tests for purchase, insufficient SP, level cap, total cap and invalid inputs.

## Not implemented yet (do not enable on live server)
- DAO transaction integration, character creation/load/save/delete and SP accrual.
- Skill purchase mapping, server-computed price table, and validation at every cast.
- Aion stat calculations, EXP/SP curve, equipment restrictions, and Chinese character level-up rewards.
- Client packet/UI changes: Aion client does not automatically display Chinese masteries.
- Import of Silkroad world/graphics or live testing. No Maven build or database migration executed.

## Safe next steps
1. Verify test runner configuration and compile/test this branch.
2. Implement DAO using database transactions and optimistic/row locking to prevent double-spend.
3. Implement a feature flag, explicit Chinese profile creation, and idempotent load/save.
4. Integrate skill eligibility in the central server-side skill restriction path, not only the UI.
5. Test Aion packet compatibility before altering classes, race enums, or map identifiers.
6. Keep the base branch untouched and use a pull request for review.
