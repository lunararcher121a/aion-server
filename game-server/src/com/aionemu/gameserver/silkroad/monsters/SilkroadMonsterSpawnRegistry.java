package com.aionemu.gameserver.silkroad.monsters;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;

/**
 * Associates a unique native spawn template with its Silkroad identity.
 * RespawnService reuses that SpawnTemplate, so overrides are reapplied to
 * each new NPC instance without touching the shared Aion NpcTemplate.
 */
public final class SilkroadMonsterSpawnRegistry {
    private static final Map<SpawnTemplate, SilkroadMonsterPrototype> BY_SPAWN =
            Collections.synchronizedMap(new WeakHashMap<>());

    private SilkroadMonsterSpawnRegistry() {
    }

    public static void register(SpawnTemplate spawn, SilkroadMonsterPrototype monster) {
        if (spawn == null || monster == null || !monster.isReadyForAdapter()
                || spawn.getNpcId() != monster.aionNpcId())
            throw new IllegalArgumentException("Invalid Silkroad spawn registration");
        BY_SPAWN.put(spawn, monster);
    }

    public static void unregister(SpawnTemplate spawn) {
        BY_SPAWN.remove(spawn);
    }

    public static void applyIfRegistered(Npc npc) {
        if (npc == null || npc.getSpawn() == null)
            return;
        SilkroadMonsterPrototype monster = BY_SPAWN.get(npc.getSpawn());
        if (monster != null) {
            npc.getGameStats().setSilkroadStats(monster);
            npc.getLifeStats().synchronizeWithMaxStats();
        }
    }
}
