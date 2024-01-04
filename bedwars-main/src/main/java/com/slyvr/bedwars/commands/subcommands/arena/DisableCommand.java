package com.slyvr.bedwars.commands.subcommands.arena;

import com.slyvr.bedwars.api.arena.Arena;
import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.game.BedwarsGame;
import com.slyvr.bedwars.utils.CommandUtils;
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

        Game arena_game = BedwarsGame.getArenaGame(arena);
        if (arena_game != null)
            arena_game.stop();

        arena.setEnabled(false);
        CommandUtils.sendArenaMessage(Message.ARENA_MODIFICATION_SETTINGS_DISABLED, player, arena);
    }

}
