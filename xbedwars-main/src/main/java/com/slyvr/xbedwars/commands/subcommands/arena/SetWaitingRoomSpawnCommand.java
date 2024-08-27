package com.slyvr.xbedwars.commands.subcommands.arena;

import com.slyvr.xbedwars.api.arena.Arena;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class SetWaitingRoomSpawnCommand extends SubCommand {

    public SetWaitingRoomSpawnCommand() {
        super("setWaitingRoomSpawn", "Sets the spawn point of the waiting-room in an arena!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw setWaitingRoomSpawn <arena>";
    }

    @Override
    public void perform(@NotNull Player player, String[] args) {
        if (args.length < 2) {
            CommandUtils.sendUsage(this, player);
            return;
        }

        Arena arena = CommandUtils.getArena(args[1], player, true);
        if (arena == null)
            return;

        arena.setWaitingRoomSpawnLocation(player.getLocation());
        CommandUtils.sendArenaMessage(Message.ARENA_MODIFICATION_SPAWNS_WAITING_ROOM, player, arena);
    }

}