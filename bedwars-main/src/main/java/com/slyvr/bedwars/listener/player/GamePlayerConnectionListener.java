package com.slyvr.bedwars.listener.player;

import com.slyvr.bedwars.Bedwars;
import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.game.BedwarsGame;
import com.slyvr.bedwars.settings.BedwarsSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;


public final class GamePlayerConnectionListener implements Listener {

    @EventHandler
    public void onGamePlayerReconnect(@NotNull PlayerJoinEvent event) {
        Game game = BedwarsGame.getDisconnectedPlayerGame(event.getPlayer());
        if (game == null)
            return;

        GamePlayer player = game.getGamePlayer(event.getPlayer());
        if (player != null)
            player.refresh();

        if (BedwarsSettings.isAutoReconnect() && event.getPlayer().hasPermission("bw.commands.rejoin"))
            game.reconnect(event.getPlayer());

        event.setJoinMessage(null);
    }

    @EventHandler
    public void onGamePlayerDisconnect(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Game game = BedwarsGame.getPlayerGame(player);
        if (game == null)
            return;

        if (!game.isRunning())
            game.getWaitingRoom().removePlayer(player);
        else
            game.disconnect(player);

        Bedwars.getInstance().getLobbiesManager().sendToRandomLobby(player);
        event.setQuitMessage(null);
    }

    private void refresh(@NotNull Player player) {
        Game game = BedwarsGame.getPlayerGame(player);
        if (game == null)
            return;

        GamePlayer game_player = game.getGamePlayer(player);
        if (game_player != null)
            game_player.refresh();
    }
}