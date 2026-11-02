# Silkroad Remastered — client assets and monster integration

## Server-first workflow
1. Use existing Aion 4.8 NPC templates/models for temporary monsters. Keep original Silkroad monster identity, level, HP, attack, defense, drops and spawn mapping in separate SRO-owned tables/config.
2. Do not reuse original Silkroad client model IDs as Aion display IDs. Maintain explicit mapping: sro_monster_id -> aion_npc_template_id -> aion_display_id.
3. Keep all new mobs opt-in in a disposable test zone until their spawns, loot, XP, AI and combat are verified.
4. Use existing Aion 4.8 motions for the first combat tests. Importing original Silkroad meshes, skeletons, animations and particles requires a separate client asset conversion pipeline and legal rights to distribute the assets.
5. Avoid promising identical skill effects or Hotan/Jangan maps without working Aion client assets.

## Client compatibility safety
- Bicheon server prototype: `-Dsilkroad.bicheon.enabled=true`.
- Custom Bicheon client skill-list projection is **off by default**. Only enable with `-Dsilkroad.bicheon.clientEnabled=true` once a compatible Aion 4.8 client has template, icon and animation support for skill 59001.
- `59001` is provisional. Aion PlayerSkillEntry.isNormalSkill() uses ID < 30000; client classification and ID collision must be checked before release.
- Server-side GM casting is not a substitute for actual client casting.

## Verification gate
- Maven Java 25 compile in GitHub Actions.
- SQL migration on disposable database; learn and relog without inserting skill 59001 into native player_skills.
- Test server-side attack, target restrictions, SP, mastery and cooldown.
- Obtain compatible client, patch skills and assets, verify skill-list display and packet cast.
- Convert a single test monster and a single animation before attempting whole Silkroad bestiary.

## Current status
No working Aion client, running game server, model conversion, animation import, or end-to-end in-game test has been verified.
