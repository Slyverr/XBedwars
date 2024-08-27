package com.slyvr.xbedwars.commands.subcommands.utils;

import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.api.arena.Arena;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.game.Game.GameState;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.game.XBedwarsGame;
import com.slyvr.xbedwars.utils.CommandUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class StopCommand extends SubCommand {

    public StopCommand() {
        super("stop", "Stops a running game!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw stop <arena/*>";
    }

    @Override
    public void perform(@NotNull Player player, @NotNull String[] args) {
        if (args.length < 2) {
            CommandUtils.sendUsage(this, player);
            return;
        }

        if (args[1].equals("*")) {
            player.sendMessage(XBedwars.PREFIX + ChatColor.GRAY + "Stopping all games!");
            XBedwarsGame.stopAll();
            return;
        }

        Arena arena = CommandUtils.getArena(args[1], player, false);
        if (arena == null)
            return;

        Game game = CommandUtils.getArenaGame(arena, player);
        if (game == null)
            return;

        GameState state = game.getState();
        if (state.ordinal() >= 3) {
            player.sendMessage(CommandUtils.format(arena, "Game isn't running!", ChatColor.RED));
            return;
        }

        if (!game.stop(true)) {
            player.sendMessage(CommandUtils.format(arena, "Could not stop the game! Please try again later!"));
            return;
        }

        player.sendMessage(CommandUtils.format(arena, "Game has successfully stopped!", ChatColor.GREEN));
    }

}