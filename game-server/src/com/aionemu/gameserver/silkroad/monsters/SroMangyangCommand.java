package com.aionemu.gameserver.silkroad.monsters;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * GM-only manual test command. Requires registration in the server's command
 * registry and an access-level entry; neither is added automatically here.
 * Spawn is placed at the invoking GM's current position.
 */
public final class SroMangyangCommand extends AdminCommand {
    public SroMangyangCommand() {
        super("sromangyang", "Spawn one test Silkroad Mangyang using an Aion NPC model",
                "spawn - Spawn one test Mangyang at your location");
    }

    @Override
    protected void execute(Player player, String... params) {
        if (params.length != 1 || !"spawn".equalsIgnoreCase(params[0]))
            throw new IllegalArgumentException("Usage: //sromangyang spawn");
        if (!Boolean.getBoolean("silkroad.monsters.enabled"))
            throw new IllegalArgumentException("Enable -Dsilkroad.monsters.enabled=true on a test server");
        if (!player.isStaff())
            throw new IllegalArgumentException("Staff only");
        var position = player.getPosition();
        Npc npc = SilkroadMonsterSpawnAdapter.spawn(MangyangTestPreset.monster(),
                MangyangTestPreset.spawnAt(player.getWorldId(),
                        position.getX(), position.getY(), position.getZ()),
                player.getInstanceId());
        sendInfo(player, "Mangyang test NPC spawned: objectId=" + npc.getObjectId()
                + ", Aion npcId=" + npc.getNpcId() + ", HP=" + npc.getLifeStats().getMaxHp());
    }
}
