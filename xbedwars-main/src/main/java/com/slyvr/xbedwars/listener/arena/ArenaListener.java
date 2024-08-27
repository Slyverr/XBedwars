package com.slyvr.xbedwars.listener.arena;

import com.slyvr.xbedwars.api.boss.GameBoss;
import com.slyvr.xbedwars.api.boss.GameBossType;
import com.slyvr.xbedwars.api.entity.GameEntity;
import com.slyvr.xbedwars.api.entity.GameEntityType;
import com.slyvr.xbedwars.listener.player.GamePlayerBlockListener;
import com.slyvr.xbedwars.settings.BedwarsSettings;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;


public final class ArenaListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == SpawnReason.CUSTOM)
            return;

        Entity entity = event.getEntity();
        if (GameEntityType.getGameEntity(entity) != null)
            return;

        if (GameBossType.getGameBoss(entity) != null)
            return;

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        event.setDroppedExp(0);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplosion(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (entity.hasMetadata("xbedwars-explosive"))
            return;

        GameEntity game_entity = GameEntityType.getGameEntity(entity);
        if (game_entity != null)
            return;

        GameBoss game_boss = GameBossType.getGameBoss(entity);
        if (game_boss != null)
            return;

        event.setCancelled(!BedwarsSettings.canEntityGrief());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityBlockChange(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();
        Block block = event.getBlock();

        GameEntity game_entity = GameEntityType.getGameEntity(entity);
        if (game_entity != null) {
            event.setCancelled(true);
            return;
        }

        GameBoss game_boss = GameBossType.getGameBoss(entity);
        if (game_boss != null) {
            GamePlayerBlockListener.addDestroyedMapBlock(game_boss.getGameTeam().getGame(), block);
            return;
        }

        event.setCancelled(!BedwarsSettings.canEntityGrief());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        Entity entity = event.getEntity();
        Block block = event.getBlock();

        GameEntity game_entity = GameEntityType.getGameEntity(entity);
        if (game_entity != null) {
            event.setCancelled(true);
            return;
        }

        GameBoss game_boss = GameBossType.getGameBoss(entity);
        if (game_boss != null) {
            GamePlayerBlockListener.addPlacedBlock(game_boss.getGameTeam().getGame(), block);
            return;
        }

        event.setCancelled(!BedwarsSettings.canEntityGrief());
    }

    // Blocks
    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        event.setCancelled(event.getCause() == IgniteCause.SPREAD);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        event.setCancelled(true);
    }

    // Items
    @EventHandler(ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        Entity entity = event.getEntity();
        if (entity.hasMetadata("xbedwars")) {
            event.setCancelled(true);
            return;
        }

        if (entity.hasMetadata("xbedwars-drop")) {
            event.setCancelled(true);
            return;
        }

        if (entity.hasMetadata("xbedwars-tiered-drop")) {
            event.setCancelled(true);
            return;
        }

        if (entity.hasMetadata("xbedwars-player-drop")) {
            event.setCancelled(true);
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onItemMerge(ItemMergeEvent event) {
        Item main = event.getEntity();
        Item target = event.getTarget();

        if (main.hasMetadata("xbedwars-drop") && target.hasMetadata("xbedwars-drop"))
            return;

        if (main.hasMetadata("xbedwars-tiered-drop") && target.hasMetadata("xbedwars-tiered-drop"))
            return;

        if (main.hasMetadata("xbedwars-player-drop") && target.hasMetadata("xbedwars-player-drop"))
            return;

        event.setCancelled(true);
    }


}