package com.slyvr.xbedwars.commands.subcommands.arena;

import com.slyvr.xbedwars.api.arena.Arena;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;


public final class SetTimeCommand extends SubCommand {

    public SetTimeCommand() {
        super("setTime", "Sets the time of the arena!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw setTime <arena> <time>";
    }

    @Override
    public void perform(@NotNull Player player, String[] args) {
        if (args.length < 3) {
            CommandUtils.sendUsage(this, player);
            return;
        }

        Arena arena = CommandUtils.getArena(args[1], player, true);
        if (arena == null)
            return;

        int time = NumberConversions.toInt(args[2]);
        if (time < 0) {
            CommandUtils.sendMessage(Message.INVALID_NUMBER, player);
            return;
        }

        arena.setTime(time);
        CommandUtils.sendArenaMessage(Message.ARENA_MODIFICATION_PREFERENCES_TIME, player, arena, time);
    }

}
