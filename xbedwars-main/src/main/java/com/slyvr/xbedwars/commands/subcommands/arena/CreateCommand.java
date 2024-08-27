package com.slyvr.xbedwars.commands.subcommands.arena;

import com.slyvr.xbedwars.api.arena.Arena;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.manager.BedwarsArenasManager;
import com.slyvr.xbedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class CreateCommand extends SubCommand {

    public CreateCommand() {
        super("create", "Creates a new arena!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw create <name>";
    }

    @Override
    public void perform(@NotNull Player player, String[] args) {
        if (args.length < 2) {
            CommandUtils.sendUsage(this, player);
            return;
        }

        Arena arena = BedwarsArenasManager.getInstance().getArena(args[1]);
        if (arena != null) {
            CommandUtils.sendMessage(Message.ARENA_EXISTS, player);
            return;
        }

        Arena created = BedwarsArenasManager.getInstance().create(args[1]);
        if (created != null)
            CommandUtils.sendArenaMessage(Message.ARENA_CREATED, player, created);
    }

}
