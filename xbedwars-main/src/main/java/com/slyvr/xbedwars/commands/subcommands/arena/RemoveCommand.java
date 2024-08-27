package com.slyvr.xbedwars.commands.subcommands.arena;

import com.slyvr.xbedwars.api.arena.Arena;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.manager.BedwarsArenasManager;
import com.slyvr.xbedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class RemoveCommand extends SubCommand {

    public RemoveCommand() {
        super("remove", "Removes an existing arena!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw remove <arena>";
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

        if (!BedwarsArenasManager.getInstance().remove(args[1]))
            CommandUtils.sendArenaMessage(Message.ARENA_RETAINED, player, arena);
        else
            CommandUtils.sendArenaMessage(Message.ARENA_REMOVED, player, arena);
    }

}