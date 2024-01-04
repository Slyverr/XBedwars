package com.slyvr.bedwars.listener.game;

import com.slyvr.bedwars.Bedwars;
import com.slyvr.bedwars.BedwarsItems;
import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.game.BedwarsGame;
import com.slyvr.bedwars.utils.MessageUtils;
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
        if (!BedwarsItems.isLobbyReturnItem(event.getItem()))
            return;

        Player player = event.getPlayer();
        Game game = BedwarsGame.getPlayerGame(player);
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
        COUNTDOWN.put(player.getUniqueId(), Bukkit.getScheduler().runTaskLater(Bedwars.getInstance(), () -> {
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