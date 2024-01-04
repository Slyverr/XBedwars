package com.slyvr.bedwars.listener.player;

import com.slyvr.bedwars.game.BedwarsGame;
import com.slyvr.bedwars.settings.BedwarsSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;

public final class GamePlayerListener implements Listener {

    public GamePlayerListener() {
    }

    @EventHandler(ignoreCancelled = true)
    public void onGamePlayerHungerLost(FoodLevelChangeEvent event) {
        if (!BedwarsSettings.canPlayerTakeDamage() || BedwarsGame.inGame((Player) event.getEntity()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onGamePlayerItemCraft(CraftItemEvent event) {
        if (BedwarsGame.inGame((Player) event.getWhoClicked()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onGamePlayerPortalEnter(PlayerPortalEvent event) {
        if (BedwarsGame.inGame(event.getPlayer()))
            event.setCancelled(true);
    }

}