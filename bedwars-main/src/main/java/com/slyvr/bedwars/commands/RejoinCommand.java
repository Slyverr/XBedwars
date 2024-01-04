package com.slyvr.bedwars.commands;

import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.game.BedwarsGame;
import com.slyvr.bedwars.utils.MessageUtils;
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

        Game game = BedwarsGame.getDisconnectedPlayerGame(player);
        if (game != null)
            game.reconnect(player);

        return true;
    }

}
