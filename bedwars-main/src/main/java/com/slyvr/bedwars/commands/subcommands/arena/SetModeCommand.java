package com.slyvr.bedwars.commands.subcommands.arena;

import com.slyvr.bedwars.api.arena.Arena;
import com.slyvr.bedwars.api.game.GameMode;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class SetModeCommand extends SubCommand {

    public SetModeCommand() {
        super("setMode", "Sets the mode of the arena!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw setMode <arena> <mode>";
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

        GameMode mode = CommandUtils.getGameMode(args[2], player);
        if (mode == null)
            return;

        arena.setMode(mode);
        CommandUtils.sendArenaMessage(Message.ARENA_MODIFICATION_SETTINGS_MODE, player, arena, mode.getName());
    }

}
