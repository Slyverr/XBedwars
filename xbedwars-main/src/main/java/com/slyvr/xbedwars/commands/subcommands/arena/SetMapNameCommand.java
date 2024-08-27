package com.slyvr.xbedwars.commands.subcommands.arena;

import com.slyvr.xbedwars.api.arena.Arena;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class SetMapNameCommand extends SubCommand {

    public SetMapNameCommand() {
        super("setMapName", "Sets the name of the arena's map!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw setMapName <arena> <name>";
    }

    @Override
    public void perform(@NotNull Player player, @NotNull String[] args) {
        if (args.length < 3) {
            CommandUtils.sendUsage(this, player);
            return;
        }

        Arena arena = CommandUtils.getArena(args[1], player, true);
        if (arena == null)
            return;

        arena.setMapName(args[2]);
        CommandUtils.sendArenaMessage(Message.ARENA_MODIFICATION_REGIONS_MAP, player, arena, args[2]);
    }

}
