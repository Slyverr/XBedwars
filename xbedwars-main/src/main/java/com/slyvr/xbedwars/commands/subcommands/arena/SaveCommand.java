package com.slyvr.xbedwars.commands.subcommands.arena;

import com.slyvr.xbedwars.api.arena.Arena;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class SaveCommand extends SubCommand {

    public SaveCommand() {
        super("save", "Save the arena!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw save <arena>";
    }

    @Override
    public void perform(@NotNull Player player, String[] args) {
        if (args.length < 2) {
            CommandUtils.sendUsage(this, player);
            return;
        }

        Arena arena = CommandUtils.getArena(args[1], player);
        if (arena == null)
            return;

        arena.saveData();
        CommandUtils.sendArenaMessage(Message.ARENA_SAVED, player, arena);
    }

}