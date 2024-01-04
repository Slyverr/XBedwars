package com.slyvr.bedwars.commands.subcommands.arena;

import com.slyvr.bedwars.api.arena.Arena;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class EnableCommand extends SubCommand {

    public EnableCommand() {
        super("enable", "Enables the arena!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw enable <arena>";
    }

    @Override
    public void perform(@NotNull Player player, String[] args) {
        if (args.length < 2) {
            CommandUtils.sendUsage(this, player);
            return;
        }

        Arena arena = CommandUtils.getArena(args[1], player, false);
        if (arena == null)
            return;

        arena.setEnabled(true);
        CommandUtils.sendArenaMessage(Message.ARENA_MODIFICATION_SETTINGS_ENABLED, player, arena);
    }

}
