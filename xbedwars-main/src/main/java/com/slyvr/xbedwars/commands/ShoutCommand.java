package com.slyvr.xbedwars.commands;

import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.api.event.player.chat.AsyncGamePlayerShoutEvent;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.game.XBedwarsGame;
import com.slyvr.xbedwars.settings.BedwarsGameSettings;
import com.slyvr.xbedwars.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public final class ShoutCommand implements CommandExecutor {

    private static final Map<UUID, Integer> COUNTDOWN = new HashMap<>();

    private static final String SHOUT_PREFIX = ChatColor.GOLD + "[SHOUT] ";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players who can execute this command!");
            return false;
        }

        if (args.length == 0)
            return true;

        Player player = (Player) sender;
        if (!player.hasPermission("bw.admin") && !player.hasPermission("bw.commands.shout")) {
            MessageUtils.sendLangMessage(Message.COMMAND_PERMISSION, player);
            return true;
        }

        Game game = XBedwarsGame.getPlayerGame(player);
        if (game == null || game.getMode().getTeamMax() == 1 || game.isSpectator(player))
            return true;

        Bukkit.getScheduler().runTaskAsynchronously(XBedwars.getInstance(), () -> {
            Integer time = ShoutCommand.COUNTDOWN.get(player.getUniqueId());
            if (time != null) {
                MessageUtils.sendLangMessage(Message.COMMAND_FAILURE_SHOUT_WAITING, player, time);
                return;
            }

            GamePlayer game_player = game.getGamePlayer(player);

            AsyncGamePlayerShoutEvent event = new AsyncGamePlayerShoutEvent(game_player, getMessage(args));
            Bukkit.getPluginManager().callEvent(event);

            String text = event.getFormat();
            text = text.replace("{team}", ChatColor.RESET + game_player.getTeamColor().getPrefix());
            text = text.replace("{player}", ChatColor.RESET + player.getDisplayName());
            text = text.replace("{message}", ChatColor.RESET + event.getMessage());

            game.broadcastMessage(MessageUtils.formatLangMessage(Message.GAME_ACTION_SHOUT, player, text));
            ShoutCommand.startCountdown(player);
        });

        return true;
    }

    private static String getMessage(@NotNull String[] args) {
        StringBuilder builder = new StringBuilder();

        for (String arg : args)
            builder.append(arg);

        return builder.toString();
    }

    private static void startCountdown(@NotNull Player player) {
        int time = BedwarsGameSettings.getShoutingTime();
        if (time <= 0)
            return;

        ShoutCommand.COUNTDOWN.put(player.getUniqueId(), BedwarsGameSettings.getShoutingTime());

        new BukkitRunnable() {

            @Override
            public void run() {
                int current = COUNTDOWN.computeIfPresent(player.getUniqueId(), (name, countdown) -> countdown - 1);
                if (current != 0)
                    return;

                this.cancel();
                ShoutCommand.COUNTDOWN.remove(player.getUniqueId());
            }
        }.runTaskTimer(XBedwars.getInstance(), 0, 20L);

    }

}