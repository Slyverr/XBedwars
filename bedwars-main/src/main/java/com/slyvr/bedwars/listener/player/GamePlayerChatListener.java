package com.slyvr.bedwars.listener.player;

import com.slyvr.bedwars.api.event.player.chat.AsyncGamePlayerChatEvent;
import com.slyvr.bedwars.api.event.room.chat.AsyncWaitingRoomPlayerChatEvent;
import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.api.room.WaitingRoom;
import com.slyvr.bedwars.api.user.User;
import com.slyvr.bedwars.api.user.level.UserLevel;
import com.slyvr.bedwars.api.user.level.UserPrestige;
import com.slyvr.bedwars.game.BedwarsGame;
import com.slyvr.bedwars.manager.BedwarsUsersManager;
import com.slyvr.bedwars.settings.BedwarsSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;


public final class GamePlayerChatListener implements Listener {

    private static final String SPECTATOR_PREFIX = ChatColor.GRAY + "[Spectator] ";

    @EventHandler
    public void onGamePlayerChat(@NotNull AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        Game game = BedwarsGame.getPlayerGame(player);
        if (game == null) {
            event.setCancelled(!BedwarsSettings.canPlayerChat() && !player.hasPermission("bw.flags.chat"));
            return;
        }

        switch (game.getState()) {
            case WAITING:
            case COUNTDOWN:
                this.onWaitingRoomChat(game.getWaitingRoom(), player, event.getMessage());
                break;
            case RUNNING:
                if (game.isSpectator(player))
                    this.onSpectatorChat(game.getGamePlayer(player), event.getMessage());
                else
                    this.onPlayerChat(game.getGamePlayer(player), event.getMessage(), false);
                break;
            case ENDING:
                this.onPlayerChat(game.getGamePlayer(player), event.getMessage(), true);
                break;
            default:
                break;
        }

        event.setCancelled(true);
    }

    private void onWaitingRoomChat(@NotNull WaitingRoom room, @NotNull Player player, @NotNull String message) {
        AsyncWaitingRoomPlayerChatEvent event = new AsyncWaitingRoomPlayerChatEvent(room, player, message);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        String format = event.getFormat();
        format = format.replace("{player}", player.getDisplayName());
        format = format.replace("{message}", message);

        room.broadcastMessage(format);
    }

    private void onSpectatorChat(@NotNull GamePlayer player, @NotNull String message) {
        String builder = SPECTATOR_PREFIX +
                ChatColor.stripColor(player.getPlayer().getDisplayName()) +
                ChatColor.GRAY +
                ": " +
                message;

        player.getGame().broadcastMessage(builder, player.getGame()::isSpectator);
    }

    private void onPlayerChat(@NotNull GamePlayer player, @NotNull String message, boolean broadcast) {
        AsyncGamePlayerChatEvent event = new AsyncGamePlayerChatEvent(player, message);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        User user = BedwarsUsersManager.getInstance().getUser(player.getPlayer());

        UserLevel level = user.hasDisplayLevel() ? user.getDisplayLevel() : user.getLevel();
        UserPrestige prestige = user.hasDisplayPrestige() ? user.getDisplayPrestige() : user.getPrestige();

        String format = event.getFormat();
        format = format.replace("{level}", prestige.formatToChat(level));
        format = format.replace("{team}", player.getTeamColor().getPrefix());
        format = format.replace("{player}", ChatColor.stripColor(player.getPlayer().getDisplayName()));
        format = format.replace("{message}", message);

        Game game = player.getGame();
        if (!broadcast && game.getMode().getTeamMax() > 1)
            game.broadcastMessage(format, p -> game.getGamePlayer(p).getTeamColor() == player.getTeamColor());
        else
            game.broadcastMessage(format);
    }

}