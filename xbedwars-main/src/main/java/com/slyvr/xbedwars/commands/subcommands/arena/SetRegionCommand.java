package com.slyvr.xbedwars.commands.subcommands.arena;

import com.slyvr.xbedwars.api.arena.Arena;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.utils.CommandUtils;
import com.slyvr.xbedwars.utils.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;


public final class SetRegionCommand extends SubCommand {

    public SetRegionCommand() {
        super("setRegion", "Sets the region of the arena!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw setRegion <arena> <X-radius> <Z-radius> <Y-max> <Y-min>";
    }

    @Override
    public void perform(@NotNull Player player, String[] args) {
        if (args.length < 6) {
            CommandUtils.sendUsage(this, player);
            return;
        }

        Arena arena = CommandUtils.getArena(args[1], player, true);
        if (arena == null)
            return;

        int x_radius = NumberConversions.toInt(args[2]);
        if (x_radius < 20) {
            CommandUtils.sendMessage(Message.INVALID_NUMBER_RADIUS, player, 'X', 20);
            return;
        }

        int z_radius = NumberConversions.toInt(args[3]);
        if (z_radius < 20) {
            CommandUtils.sendMessage(Message.INVALID_NUMBER_RADIUS, player, 'Z', 20);
            return;
        }

        int y_max = NumberConversions.toInt(args[4]);
        int y_min = NumberConversions.toInt(args[5]);

        if (y_max < y_min) {
            CommandUtils.sendMessage(Message.INVALID_NUMBER_RANGE, player, "Y-max", "Y-min");
            return;
        }

        Location pos1 = player.getLocation().add(x_radius, 0, z_radius);
        pos1.setY(y_max);

        Location pos2 = player.getLocation().subtract(x_radius, 0, z_radius);
        pos2.setY(y_min);

        arena.setRegion(new Region(pos1, pos2));
        CommandUtils.sendArenaMessage(Message.ARENA_MODIFICATION_REGIONS_MAP, player, arena);
    }

}