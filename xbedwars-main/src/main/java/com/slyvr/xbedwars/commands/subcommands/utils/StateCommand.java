package com.slyvr.xbedwars.commands.subcommands.utils;

import com.slyvr.xbedwars.api.arena.Arena;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.game.Game.GameState;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.utils.CommandUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class StateCommand extends SubCommand {

    public StateCommand() {
        super("state", "Shows the state of a game!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw state <arena>";
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

        Game game = CommandUtils.getArenaGame(arena, player);
        if (game == null)
            return;

        player.sendMessage(CommandUtils.format(arena, getStateMessage(game.getState())));
    }

    private String getStateMessage(GameState state) {
        return "State of the game: " + state.getChatColor() + state + ChatColor.GRAY + "!";
    }

}
