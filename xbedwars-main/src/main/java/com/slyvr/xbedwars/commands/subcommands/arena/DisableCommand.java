package com.slyvr.xbedwars.commands.subcommands.arena;

import com.slyvr.xbedwars.api.arena.Arena;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.game.XBedwarsGame;
import com.slyvr.xbedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class DisableCommand extends SubCommand {

    public DisableCommand() {
        super("disable", "Disables the arena!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw disable <arena>";
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

        Game arena_game = XBedwarsGame.getArenaGame(arena);
        if (arena_game != null)
            arena_game.stop();

        arena.setEnabled(false);
        CommandUtils.sendArenaMessage(Message.ARENA_MODIFICATION_SETTINGS_DISABLED, player, arena);
    }

}
