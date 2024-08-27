package com.slyvr.xbedwars.commands;

import com.slyvr.xbedwars.api.game.GameMode;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.manager.ArenasManager;
import com.slyvr.xbedwars.game.XBedwarsGame;
import com.slyvr.xbedwars.manager.BedwarsArenasManager;
import com.slyvr.xbedwars.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class PlayCommand implements CommandExecutor {

    private final ArenasManager manager = BedwarsArenasManager.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player) || args.length == 0)
            return true;

        Player player = (Player) sender;
        if (!player.hasPermission("bw.admin") && !player.hasPermission("bw.commands.play")) {
            MessageUtils.sendLangMessage(Message.COMMAND_PERMISSION, player);
            return true;
        }

        GameMode mode = GameMode.getByPlayCommand(args[0]);
        if (mode == null)
            return true;

        if (XBedwarsGame.addToRandomGame(player, game -> mode.equals(game.getMode())) != null)
            return true;

        if (XBedwarsGame.addToRandomArena(player, arena -> mode.equals(arena.getMode()) && arena.isReady()) != null)
            return true;

        MessageUtils.sendLangMessage(Message.COMMAND_FAILURE_PLAY_GAME_MISSING, player);
        return true;
    }

}
