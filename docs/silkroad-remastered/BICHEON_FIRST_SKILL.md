# Bicheon Smash A — first Aion 4.8 server prototype

**Status: server-side building blocks, NOT playable in the Aion client yet.**
Only the `silkroad-remastered` branch is modified. No migration was executed.

## Verified OpenSRO v1.150 metadata
- Silkroad skill id: **3**
- Name: `SKILL_CH_SWORD_SMASH_A_01`
- Chinese mastery: **Bicheon 257**, required level **5**
- Training cost: **2 SP** (spent ONCE on learning, not per attack)
- Skill group: **174**, rank **1**

## Implemented
- `BicheonSmashPrototype`: pure, testable skill learning/casting gate.
- `calculatePrototypeBonus`: a capped 11%-30% physical attack bonus for
  local tuning, **not** a verified Silkroad damage formula and **not**
  directly applied to live target HP.
- `BicheonAionSkillBridge`: prepares an existing Aion Skill object only
  when the caller has authenticated and authorized the action, and the
  target Aion skill template and learned skill exist. It never uses Silkroad
  id 3 as an Aion id.
- JUnit tests cover SP, mastery, learned status, weapon, hostile target,
  MP, cooldown and prototype damage bonus.

## Required before it works in game
1. Implement transactional DAO for `sro_characters`, `sro_masteries`
   and `sro_learned_skills`; debit SP and grant learned rank atomically.
2. Add a feature flag and load persisted Chinese profile on login.
3. Bind a **new Aion skill template id verified unused** in both server
   templates and matching client resources. Do not reuse Aion skill id 3.
4. Configure physical damage via Aion `SkillAttackInstantEffect`, with
   server-controlled cost/cooldown and correct target/weapon conditions.
5. At the authenticated Aion skill-use handler, derive all cast predicates
   from server state; NEVER accept client-provided `READY` authorization.
6. Route through Aion's normal Skill.useSkill() after restrictions; ensure
   normal damage calculation, PvP and death systems are preserved.
7. Test compile, JUnit, disposable SQL migration, and real Aion 4.8 client
   (skill icon, animation, packets and hit result).

The bridge deliberately does not bypass the existing Aion player skill list.
The first Aion XML template is blocked on client-compatible id/motion selection.
