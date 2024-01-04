package com.slyvr.bedwars.listener.hologram;

import com.slyvr.bedwars.game.BedwarsGame;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.jetbrains.annotations.NotNull;


public final class HologramListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onArmorStandInteract(@NotNull PlayerArmorStandManipulateEvent event) {
        if (BedwarsGame.inGame(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorStandDamage(@NotNull EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.ARMOR_STAND)
            event.setCancelled(true);
    }

}