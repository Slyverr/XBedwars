package com.slyvr.xbedwars.listener.game;

import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.XBedwarsItems;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.game.XBedwarsGame;
import com.slyvr.xbedwars.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public final class GameWaitingRoomListener implements Listener {

    private static final Map<UUID, BukkitTask> COUNTDOWN = new HashMap<>();

    @EventHandler
    public void onLobbyReturnItemClick(@NotNull PlayerInteractEvent event) {
        if (!XBedwarsItems.isLobbyReturnItem(event.getItem()))
            return;

        Player player = event.getPlayer();
        Game game = XBedwarsGame.getPlayerGame(player);
        if (game == null || game.isRunning())
            return;

        BukkitTask task = COUNTDOWN.get(player.getUniqueId());
        if (task == null)
            this.addToCountdown(game, player);
        else
            this.removeFromCountdown(player);

        event.setCancelled(true);
    }

    private void addToCountdown(@NotNull Game game, @NotNull Player player) {
        COUNTDOWN.put(player.getUniqueId(), Bukkit.getScheduler().runTaskLater(XBedwars.getInstance(), () -> {
            game.getWaitingRoom().removePlayer(player);
            COUNTDOWN.remove(player.getUniqueId());
        }, 60L));

        MessageUtils.sendLangMessage(Message.LOBBY_TELEPORT_WAITING, player);
        MessageUtils.sendLangMessage(Message.LOBBY_TELEPORT_HINT, player);
    }

    private void removeFromCountdown(Player player) {
        BukkitTask task = COUNTDOWN.remove(player.getUniqueId());
        task.cancel();

        MessageUtils.sendLangMessage(Message.LOBBY_TELEPORT_CANCELLED, player);
    }

}