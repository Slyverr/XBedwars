package com.slyvr.bedwars.commands.subcommands.arena;

import com.slyvr.bedwars.api.arena.Arena;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.utils.CommandUtils;
import com.slyvr.bedwars.utils.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;


public final class SetWaitingRoomRegionCommand extends SubCommand {

    public SetWaitingRoomRegionCommand() {
        super("setWaitingRoomRegion", "Sets the region of the waiting-room in an arena!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw setWaitingRegion <arena> <X-radius> <Y-radius> <Z-radius>";
    }

    @Override
    public void perform(@NotNull Player player, String[] args) {
        if (args.length < 5) {
            CommandUtils.sendUsage(this, player);
            return;
        }

        Arena arena = CommandUtils.getArena(args[1], player, true);
        if (arena == null)
            return;

        int x_radius = NumberConversions.toInt(args[2]);
        if (x_radius < 5) {
            CommandUtils.sendMessage(Message.INVALID_NUMBER_RADIUS, player, 'X', 5);
            return;
        }


        int y_radius = NumberConversions.toInt(args[3]);
        if (y_radius < 5) {
            CommandUtils.sendMessage(Message.INVALID_NUMBER_RADIUS, player, 'Y', 5);
            return;
        }

        int z_radius = NumberConversions.toInt(args[4]);
        if (z_radius < 5) {
            CommandUtils.sendMessage(Message.INVALID_NUMBER_RADIUS, player, 'Z', 5);
            return;
        }

        Location pos1 = player.getLocation().add(x_radius, y_radius, z_radius);
        Location pos2 = player.getLocation().subtract(x_radius, y_radius, z_radius);

        arena.setWaitingRoomRegion(new Region(pos1, pos2));
        CommandUtils.sendArenaMessage(Message.ARENA_MODIFICATION_REGIONS_WAITING_ROOM, player, arena);
    }

}