package com.slyvr.xbedwars.commands;

import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.game.XBedwarsGame;
import com.slyvr.xbedwars.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class RejoinCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players who can execute this command!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("bw.admin") && !player.hasPermission("bw.commands.rejoin")) {
            MessageUtils.sendLangMessage(Message.COMMAND_PERMISSION, player);
            return true;
        }

        Game game = XBedwarsGame.getDisconnectedPlayerGame(player);
        if (game != null)
            game.reconnect(player);

        return true;
    }

}
