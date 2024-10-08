package com.slyvr.xbedwars.listener.player;

import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.game.XBedwarsGame;
import com.slyvr.xbedwars.settings.BedwarsSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;


public final class GamePlayerConnectionListener implements Listener {

    @EventHandler
    public void onGamePlayerReconnect(@NotNull PlayerJoinEvent event) {
        Game game = XBedwarsGame.getDisconnectedPlayerGame(event.getPlayer());
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
        Game game = XBedwarsGame.getPlayerGame(player);
        if (game == null)
            return;

        if (!game.isRunning())
            game.getWaitingRoom().removePlayer(player);
        else
            game.disconnect(player);

        XBedwars.getInstance().getLobbiesManager().sendToRandomLobby(player);
        event.setQuitMessage(null);
    }

    private void refresh(@NotNull Player player) {
        Game game = XBedwarsGame.getPlayerGame(player);
        if (game == null)
            return;

        GamePlayer game_player = game.getGamePlayer(player);
        if (game_player != null)
            game_player.refresh();
    }
}