package com.slyvr.xbedwars.listener.player;

import com.cryptomorin.xseries.XMaterial;
import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.api.arena.team.ArenaTeam;
import com.slyvr.xbedwars.api.entity.GameEntityType;
import com.slyvr.xbedwars.api.entity.GameEntityType.SpawnType;
import com.slyvr.xbedwars.api.event.player.interaction.GamePlayerInteractEvent;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.api.team.TeamColor;
import com.slyvr.xbedwars.game.XBedwarsGame;
import com.slyvr.xbedwars.listener.entity.GameEntityListener;
import com.slyvr.xbedwars.settings.BedwarsGameSettings;
import com.slyvr.xbedwars.settings.BedwarsSettings;
import com.slyvr.xbedwars.utils.BedUtils;
import com.slyvr.xbedwars.utils.ListenerUtils;
import com.slyvr.xbedwars.utils.MessageUtils;
import com.slyvr.xbedwars.utils.Version;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GamePlayerInteractListener implements Listener {

    @EventHandler
    public void onGamePlayerBlockRightClick(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        Game game = XBedwarsGame.getPlayerGame(player);
        if (game == null) {
            event.setCancelled(!BedwarsSettings.canPlayerInteractWithBlock() && !player.hasPermission("bw.flags.block.interact"));
            return;
        }

        if (!game.isRunning() || game.isSpectator(player)) {
            event.setCancelled(true);
            return;
        }

        GamePlayer game_player = game.getGamePlayer(player);

        GamePlayerInteractEvent bwEvent = new GamePlayerInteractEvent(game_player, Action.RIGHT_CLICK_BLOCK, event.getItem(), getEquipmentSlot(event), block, event.getBlockFace());
        Bukkit.getPluginManager().callEvent(bwEvent);

        Event.Result useItem = bwEvent.useItemInHand();
        Event.Result useBlock = bwEvent.useClickedBlock();

        if (useBlock != Event.Result.DENY) {
            if (block.getType() == Material.CHEST) {
                this.handleGamePlayerChestOpen(game_player, event, block);
                return;
            }
        }

        if (useItem != Event.Result.DENY && event.hasItem()) {
            ItemStack item = event.getItem();
            Material type = item.getType();

            if (type == XMaterial.FIRE_CHARGE.parseMaterial()) {
                this.handleGamePlayerFireballLaunch(game_player, event);
                return;
            }

            if (type == Material.WATER_BUCKET || type == Material.LAVA_BUCKET) {
                this.handleGamePlayerBucketUse(game_player, event);
                return;
            }

            if (type.isBlock() && BedUtils.isBed(block.getType())) {
                event.setUseItemInHand(Result.ALLOW);
                event.setUseInteractedBlock(Result.DENY);
                // this.handleGamePlayerBlockPlace();
                return;
            }

            GameEntityType<?> entity_type = GameEntityType.getByItem(item);
            if (entity_type != null) {
                this.handleGamePlayerEntitySpawn(game_player, event, entity_type);
                return;
            }
        }

        event.setUseItemInHand(useItem);
        event.setUseInteractedBlock(useBlock);
    }

    @EventHandler
    public void onGamePlayerAirRightClick(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR)
            return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        Game game = XBedwarsGame.getPlayerGame(player);
        if (game == null) {
            event.setCancelled(!BedwarsSettings.canPlayerInteractWithBlock() && !player.hasPermission("bw.flags.block.interact"));
            return;
        }

        if (!game.isRunning() || game.isSpectator(player)) {
            event.setCancelled(true);
            return;
        }

        GamePlayer game_player = game.getGamePlayer(player);

        GamePlayerInteractEvent bwEvent = new GamePlayerInteractEvent(game_player, Action.RIGHT_CLICK_AIR, item, getEquipmentSlot(event), null, event.getBlockFace());
        Bukkit.getPluginManager().callEvent(bwEvent);

        Event.Result useItem = bwEvent.useItemInHand();
        Event.Result useBlock = bwEvent.useClickedBlock();

        if (useItem != Event.Result.DENY && event.hasItem()) {
            Material type = event.getMaterial();

            if (type == XMaterial.FIRE_CHARGE.parseMaterial()) {
                this.handleGamePlayerFireballLaunch(game_player, event);
                return;
            }

            GameEntityType<?> entity_type = GameEntityType.getByItem(item);
            if (entity_type != null && entity_type.getSpawnType() == SpawnType.PROJECTILE) {
                this.handleGamePlayerEntitySpawn(game_player, event, entity_type);
                return;
            }

        }

        event.setUseItemInHand(useItem);
        event.setUseInteractedBlock(useBlock);
    }

    @EventHandler
    public void onGamePlayerInteract(@NotNull PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)
            return;

        Player player = event.getPlayer();

        Game game = XBedwarsGame.getPlayerGame(player);
        if (game == null) {
            event.setCancelled(!BedwarsSettings.canPlayerInteractWithBlock() && !player.hasPermission("bw.flags.block.interact"));
            return;
        }

        if (!game.isRunning() || game.isSpectator(player)) {
            event.setCancelled(true);
            return;
        }

        GamePlayerInteractEvent bwEvent = new GamePlayerInteractEvent(game.getGamePlayer(player), event.getAction(), event.getItem(), getEquipmentSlot(event), event.getClickedBlock(), event.getBlockFace());
        Bukkit.getPluginManager().callEvent(bwEvent);

        event.setUseItemInHand(bwEvent.useItemInHand());
        event.setUseInteractedBlock(bwEvent.useClickedBlock());
    }

    @EventHandler
    public void onGamePlayerBedEnter(@NotNull PlayerBedEnterEvent event) {
        if (XBedwarsGame.inGame(event.getPlayer()))
            event.setCancelled(true);
    }

    private void handleGamePlayerBucketUse(@NotNull GamePlayer player, @NotNull PlayerInteractEvent event) {
        Block block = event.getClickedBlock().getRelative(event.getBlockFace());

        switch (event.getMaterial()) {
            case WATER_BUCKET:
                block.setType(Material.WATER);
                break;
            case LAVA_BUCKET:
                block.setType(Material.LAVA);
                break;
        }

        GamePlayerBlockListener.addPlacedBlock(player.getGame(), block);

        ListenerUtils.decrementItemInHandAmount(player.getPlayer(), isRightHand(event));
        event.setCancelled(true);
    }

    private void handleGamePlayerFireballLaunch(@NotNull GamePlayer player, @NotNull PlayerInteractEvent event) {
        Player bukkit_player = player.getPlayer();
        Vector velocity = bukkit_player.getEyeLocation().getDirection().normalize().multiply(BedwarsGameSettings.getFireballSpeed());

        Fireball fb = bukkit_player.launchProjectile(Fireball.class, velocity);
        fb.setMetadata("xbedwars-explosive", new FixedMetadataValue(XBedwars.getInstance(), player));
        fb.setIsIncendiary(BedwarsGameSettings.isFireballIncendiary());
        fb.setYield(BedwarsGameSettings.getFireballExplosionPower());
        fb.setBounce(BedwarsGameSettings.isFireballBouncing());

        ListenerUtils.decrementItemInHandAmount(bukkit_player, isRightHand(event));
        event.setCancelled(true);
    }

    private void handleGamePlayerEntitySpawn(@NotNull GamePlayer player, @NotNull PlayerInteractEvent event, @NotNull GameEntityType<?> type) {
        if (!GameEntityListener.canSpawn(player, type)) {
            MessageUtils.sendLangMessage(Message.GAME_ENTITY_SPAWN_RESTRICTED_LIMIT, player.getPlayer());
            event.setCancelled(true);
            return;
        }

        Player bukkit_player = player.getPlayer();
        switch (type.getSpawnType()) {
            case SPAWN_EGG:
                GameEntityListener.spawn(player, type, event.getClickedBlock().getLocation().add(0.5, 1, 0.5));
                break;
            case PROJECTILE:
                GameEntityListener.launch(player, type);
                break;
        }

        ListenerUtils.decrementItemInHandAmount(bukkit_player, isRightHand(event));
        event.setCancelled(true);
    }

    private void handleGamePlayerChestOpen(@NotNull GamePlayer player, @NotNull PlayerInteractEvent event, @NotNull Block block) {
        TeamColor player_color = player.getTeamColor();
        Player bukkit_player = player.getPlayer();

        Game game = player.getGame();
        for (ArenaTeam team : game.getArena().getReadyTeams()) {
            Chest chest = team.getChest();
            if (chest == null || !chest.getBlock().equals(block))
                continue;

            TeamColor color = team.getColor();
            if (color == player.getTeamColor() || game.isEliminated(color))
                return;

            MessageUtils.sendLangMessage(Message.BLOCK_CHEST_RESTRICTED, bukkit_player, color.getColoredName());
            event.setCancelled(true);
            return;
        }

    }

    @Nullable
    private EquipmentSlot getEquipmentSlot(@NotNull PlayerInteractEvent event) {
        return Version.getVersion().isNewerThan(Version.V1_8_R3) ? event.getHand() : null;
    }

    private boolean isRightHand(@NotNull PlayerInteractEvent event) {
        return !Version.getVersion().isNewerThan(Version.V1_8_R3) || event.getHand() != EquipmentSlot.OFF_HAND;
    }

}