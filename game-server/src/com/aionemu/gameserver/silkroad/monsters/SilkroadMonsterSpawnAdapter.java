package com.aionemu.gameserver.silkroad.monsters;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.spawnengine.SpawnEngine;

/**
 * Explicit opt-in bridge to native Aion NPC spawning.
 * The mapped NPC supplies its native Aion mesh, AI and animations.
 * Silkroad combat stats are applied to the individual NPC through the spawn registry;
 * the shared Aion NPC template remains unchanged.
 */
public final class SilkroadMonsterSpawnAdapter {
    private SilkroadMonsterSpawnAdapter() {
    }

    public static Npc spawn(SilkroadMonsterPrototype monster, SilkroadMonsterSpawn spawn, int instanceId) {
        if (!Boolean.getBoolean("silkroad.monsters.enabled"))
            throw new IllegalStateException("Silkroad monster spawning is disabled");
        if (monster == null || spawn == null || !monster.isReadyForAdapter() || !spawn.enabled())
            throw new IllegalArgumentException("Monster and spawn must be enabled");
        if (!monster.silkroadCode().equals(spawn.silkroadCode()))
            throw new IllegalArgumentException("Spawn belongs to another monster");
        if (instanceId < 0)
            throw new IllegalArgumentException("Invalid instance ID");
        int npcId = monster.aionNpcId();
        if (DataManager.NPC_DATA == null || DataManager.NPC_DATA.getNpcTemplate(npcId) == null)
            throw new IllegalArgumentException("Unknown Aion NPC template: " + npcId);
        if (DataManager.WORLD_MAPS_DATA == null
                || DataManager.WORLD_MAPS_DATA.getTemplate(spawn.worldId()) == null)
            throw new IllegalArgumentException("Unknown Aion world: " + spawn.worldId());

        SpawnTemplate template = SpawnEngine.newSpawn(spawn.worldId(), npcId,
                spawn.x(), spawn.y(), spawn.z(), (byte) 0, spawn.respawnSeconds());
        SilkroadMonsterSpawnRegistry.register(template, monster);
        VisibleObject object;
        try {
            object = SpawnEngine.spawnObject(template, instanceId);
        } catch (RuntimeException ex) {
            SilkroadMonsterSpawnRegistry.unregister(template);
            throw ex;
        }
        if (!(object instanceof Npc npc) || !npc.isSpawned()) {
            SilkroadMonsterSpawnRegistry.unregister(template);
            throw new IllegalStateException("Aion NPC spawn failed: " + npcId);
        }
        return npc;
    }
}
