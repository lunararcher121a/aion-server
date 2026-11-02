# OpenSRO v1.150 vs Aion 4.8 — Chinese skills

Status: **data-only groundwork committed; NOT live gameplay or client animation support.**
Everything in this document refers to the `silkroad-remastered` branch of
`lunararcher121a/aion-server`; the original `4.8` branch is unchanged.

## Source-of-truth audit

OpenSRO `lunararcher121a/opensro`, `main`:

- `apps/server/internal/domain/progression.go` — native Chinese mastery ids
  `257 Bicheon, 258 Heuksal, 259 Pacheon, 273 Cold, 274 Lightning,
  275 Fire, 276 Force`. The server currently stores local ids 1..7
  for SQL compatibility and converts using `getSilkroadId()`.
- `apps/server/internal/game/enterworld/defaultskills_test.go` — authentic
  v1.150 starter ids `1 Punch, 2 Sword Base, 40 Spear Base, 70 Bow Base`,
  all rank 1 and 0 SP.
- `apps/server/internal/game/progression/skilllearn_test.go` — verified
  Bicheon `Smash A` id 3, group 174, mastery 257@5, cost 2 SP;
  `Smash B` id 4, group 175, mastery 257@27, group 174 rank 9
  prerequisite, cost 117 SP. Also describes chain root id 6, group 177,
  requirement 257@7, cost 5 SP; next stages 7 and 8.
  The full version-specific codename of root 6 was NOT established from the
  checked source; it is deliberately not registered in the initial catalogue.
- `apps/server/internal/game/progression/runtime.go` — CH total mastery
  cap 300; gates check SP, mastery and prerequisite under a serialized commit.
- `apps/server/internal/game/enterworld/skilldata.go` — client data columns
  encode two mastery requirements, SP cost and skill-group prerequisites.
- `scripts/build/char/parseSkillEffect.mjs` — joins skill data to animation
  sets by their authored codenames; numeric ids may drift across versions.
- `scripts/build/char/compileBanTimeline.mjs` — BAN animation timeline data.
- `scripts/build/effects/buildEffectPrograms.mjs` — EFP resource processing;
  the raw client asset files are not committed to OpenSRO.

Aion `lunararcher121a/aion-server`, `4.8`:

- `skillengine/SkillEngine.java` resolves **Aion** SkillTemplate ids and
  checks PlayerSkillList for a learned Aion skill.
- `skillengine/model/SkillTemplate.java` stores activation, cooldown,
  conditions, effects, and motion metadata.
- `controllers/PlayerController.java` uses SkillEngine and
  PlayerRestrictions before casting.
- `restrictions/PlayerRestrictions.java` contains the common cast gate.
- `skillengine/effect/EffectType.java` includes
  SKILLATTACKINSTANT, SPELLATTACKINSTANT, STUN, SLOW, SNARE, STATUP,
  HEALINSTANT, DISPELDEBUFF etc.
- `data/static_data/skills/skill_templates.xml` already uses Aion ids
  **1, 2, 3...** for Aion-specific skills. **Never** substitute Silkroad
  numeric ids into this table or call SkillEngine with them unmodified.

## Prepared compatibility components

- `ChineseMastery`: local <-> native v1.150 mastery id translation.
- `ChineseSkillCatalog`: small verified skill sample with groups, SP costs,
  requirements and starting skill identities.
- `ChineseSkillEligibility`: pure, fail-closed training preflight; caller
  MUST recompute checks inside the same persistence transaction as spending SP.
- `ChineseAionEffectPlan`: **candidate** effect classes per school. These are
  not 1:1 equivalents; knockback, physical/magic formulae, chained hit timing,
  ranged attack motion and imbuements all need specific implementation.
  **No Aion SkillTemplate id is bound.** All actual client bindings are disabled.

## Next engineering steps (not yet done)

1. Ingest all relevant original v1.150 `skilldata*.txt` plus
   `skillmasterydata.txt` from a legally obtained matching client, using
   codename keys and recorded provenance. Do not invent missing rows.
2. Build a skill catalogue importer with strict validation (spells, required
   weapons, ranks, masteries, cooldowns, status effects, prerequisites,
   client effect records) and automatic tests for collisions.
3. Implement atomic SQL update that deducts SP and adds/replaces learned
   ranks; protect against repeated packets, concurrency and disconnect.
4. Expose an opt-in per-character Chinese mode, populate the four starter
   skills, and disable Aion auto-learning for only those characters.
5. Create **new client-verified Aion skill ids** and skill templates, plus
   animations, icons and UI. These require actual Aion client resource work.
6. Integrate cast-time checks in the existing Aion restriction path and
   compare physical/magical damage for each confirmed Silkroad skill family.
7. Run Maven/JUnit, migration tests against disposable MariaDB,
   and client/server end-to-end tests before any public deployment.

## Licensing and assets

OpenSRO's software uses AGPL-3.0-or-later. This implementation is newly
written and uses factual game metadata as a compatibility reference, not a
copy of OpenSRO implementation source. Review legal obligations before
incorporating OpenSRO code into distributed or publicly hosted software.
Neither this branch nor OpenSRO publishes retail Silkroad animation files.
