package com.slyvr.bedwars.listener.arena;

import com.slyvr.bedwars.api.boss.GameBoss;
import com.slyvr.bedwars.api.boss.GameBossType;
import com.slyvr.bedwars.api.entity.GameEntity;
import com.slyvr.bedwars.api.entity.GameEntityType;
import com.slyvr.bedwars.listener.player.GamePlayerBlockListener;
import com.slyvr.bedwars.settings.BedwarsSettings;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;


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
        if (entity.hasMetadata("bedwars-explosive"))
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
        if (entity.hasMetadata("bedwars")) {
            event.setCancelled(true);
            return;
        }

        if (entity.hasMetadata("bedwars-drop")) {
            event.setCancelled(true);
            return;
        }

        if (entity.hasMetadata("bedwars-tiered-drop")) {
            event.setCancelled(true);
            return;
        }

        if (entity.hasMetadata("bedwars-player-drop")) {
            event.setCancelled(true);
            return;
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onItemMerge(ItemMergeEvent event) {
        Item main = event.getEntity();
        Item target = event.getTarget();

        if (main.hasMetadata("bedwars-drop") && target.hasMetadata("bedwars-drop"))
            return;

        if (main.hasMetadata("bedwars-tiered-drop") && target.hasMetadata("bedwars-tiered-drop"))
            return;

        if (main.hasMetadata("bedwars-player-drop") && target.hasMetadata("bedwars-player-drop"))
            return;

        event.setCancelled(true);
    }


}