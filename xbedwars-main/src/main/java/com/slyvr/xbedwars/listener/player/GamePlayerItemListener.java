package com.slyvr.xbedwars.listener.player;

import com.cryptomorin.xseries.XMaterial;
import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.XBedwarsItems;
import com.slyvr.xbedwars.api.arena.generator.ArenaResourceGeneratorManager;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.api.player.GamePlayerInventory;
import com.slyvr.xbedwars.api.team.GameTeam;
import com.slyvr.xbedwars.game.XBedwarsGame;
import com.slyvr.xbedwars.settings.BedwarsSettings;
import com.slyvr.xbedwars.shop.ShopInventory;
import com.slyvr.xbedwars.utils.ListenerUtils;
import com.slyvr.xbedwars.utils.MessageUtils;
import com.slyvr.xbedwars.utils.Version;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public final class GamePlayerItemListener implements Listener {

    private static final MetadataValue EMPTY_METADATA = new FixedMetadataValue(XBedwars.getInstance(), null);

    private static final Map<Game, GameStorage> GAMES_STORAGES = new HashMap<>();

    public GamePlayerItemListener() {
    }

    @EventHandler(ignoreCancelled = true)
    public void onGamePlayerItemDrop(@NotNull PlayerDropItemEvent event) {
        Game game = XBedwarsGame.getPlayerGame(event.getPlayer());
        if (game == null) {
            event.setCancelled(!BedwarsSettings.canPlayerDropItem() && !event.getPlayer().hasPermission("bw.flags.item.drop"));
            return;
        }

        if (!game.isRunning() || game.isSpectator(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }

        GamePlayer player = game.getGamePlayer(event.getPlayer());
        GamePlayerInventory inv = player.getInventory();

        Item item = event.getItemDrop();
        if (inv.contains(item.getItemStack().getType())) {
            event.setCancelled(true);
            return;
        }

        GamePlayerItemListener.addDroppedItem(game, item);
        GamePlayerItemListener.checkForSword(player);
        GamePlayerItemListener.updateTeam(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onGamePlayerItemPickup(@NotNull PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();

        Game game = XBedwarsGame.getPlayerGame(player);
        if (game == null) {
            event.setCancelled(!BedwarsSettings.canPlayerPickUpItem() && !player.hasPermission("bw.flags.item.pickup"));
            return;
        }

        if (!game.isRunning() || game.isSpectator(player)) {
            event.setCancelled(true);
            return;
        }

        if (isOtherSwordType(item.getItemStack().getType()))
            player.getInventory().remove(XBedwarsItems.SWORD);

        ArenaResourceGeneratorManager manager = game.getArena().getResourceGeneratorManager();
        GamePlayer game_player = game.getGamePlayer(player);

        if (manager.isTeamResourceGeneratorSplitting() && item.hasMetadata("xbedwars-drop")) {
            ItemStack stack = item.getItemStack();

            double radius = manager.getTeamResourceGeneratorSplittingRadius();
            for (Entity nearby : item.getNearbyEntities(radius, radius, radius)) {
                if (!(nearby instanceof Player) || nearby.equals(player))
                    continue;

                Player nearby_player = (Player) nearby;
                if (game.isSpectator(nearby_player))
                    continue;

                GamePlayer nearby_game_player = game.getGamePlayer(nearby_player);
                if (nearby_game_player != null && nearby_game_player.getTeamColor() == game_player.getTeamColor())
                    nearby_player.getInventory().addItem(stack);
            }

        }

        GamePlayerItemListener.updateTeam(game_player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onGamePlayerItemConsume(@NotNull PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        Game game = XBedwarsGame.getPlayerGame(player);
        if (game == null)
            return;

        if (!game.isRunning() || game.isSpectator(player)) {
            event.setCancelled(true);
            return;
        }
        GamePlayer game_player = game.getGamePlayer(player);
        Material item_type = item.getType();

        if (item_type == Material.MILK_BUCKET) {
            this.handleGamePlayerMagicMilkConsume(game_player, event);
            return;
        }

        if (item_type == Material.POTION) {
            this.handleGamePlayerPotionConsume(game_player, event);
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onGamePlayerInventoryClick(@NotNull InventoryClickEvent event) {
        if (event.getSlotType() == SlotType.OUTSIDE)
            return;

        Inventory inv = event.getInventory();
        if (ShopInventory.isShopInventory(inv))
            return;

        Player player = (Player) event.getWhoClicked();
        Game game = XBedwarsGame.getPlayerGame(player);
        if (game == null)
            return;

        if (!game.isRunning() || game.isSpectator(player)) {
            event.setCancelled(true);
            return;
        }

        if (event.getSlotType() == SlotType.ARMOR) {
            event.setCancelled(true);
            return;
        }

        GamePlayer gp = game.getGamePlayer(player);
        GamePlayerInventory gp_inv = gp.getInventory();

        ItemStack current = event.getCurrentItem();
        if (current != null && gp_inv.contains(current.getType()))
            return;

        Bukkit.getScheduler().runTaskLater(XBedwars.getInstance(), () -> {
            Inventory player_inv = player.getInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (item == null)
                    continue;

                if (!gp_inv.contains(item.getType()))
                    continue;

                if (player_inv.contains(item.getType()))
                    continue;

                inv.setItem(i, null);
                player_inv.addItem(item);
            }

            GamePlayerItemListener.checkForSword(gp);
            GamePlayerItemListener.updateTeam(gp);
        }, 5L);

    }

    private void handleGamePlayerMagicMilkConsume(@NotNull GamePlayer player, @NotNull PlayerItemConsumeEvent event) {
        Player bukkit_player = player.getPlayer();

        ListenerUtils.decrementItemInHandAmount(bukkit_player, isRightHand(event));
        MessageUtils.sendLangMessage(Message.PLAYER_PERK_MAGIC_MILK, bukkit_player);

        event.setCancelled(true);
        player.setTrapSafe(XBedwars.getInstance(), true);

        Bukkit.getScheduler().runTaskLater(XBedwars.getInstance(), () -> player.setTrapSafe(XBedwars.getInstance(), false), 30 * 20L);
    }

    private void handleGamePlayerPotionConsume(@NotNull GamePlayer player, @NotNull PlayerItemConsumeEvent event) {
        for (PotionEffect effect : getEffects(event.getItem())) {
            if (!player.getPlayer().addPotionEffect(effect))
                continue;

            if (effect.getType() != PotionEffectType.INVISIBILITY)
                continue;

            player.setInvisible(XBedwars.getInstance(), true);
            Bukkit.getScheduler().runTaskLater(XBedwars.getInstance(), () -> player.setInvisible(XBedwars.getInstance(), false), effect.getDuration());
        }

        ListenerUtils.decrementItemInHandAmount(player.getPlayer(), isRightHand(event));
        event.setCancelled(true);
    }

    @Nullable
    private Collection<PotionEffect> getEffects(@NotNull ItemStack item) {
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta.hasCustomEffects())
            return meta.getCustomEffects();

        Potion potion = Potion.fromItemStack(item);
        return potion.getEffects();
    }

    private boolean isRightHand(@NotNull PlayerItemConsumeEvent event) {
        if (!Version.getVersion().isNewerThan(Version.V1_8_R3))
            return true;

        PlayerInventory inv = event.getPlayer().getInventory();

        ItemStack item = inv.getItemInMainHand();
        return item != null && item.equals(event.getItem());
    }

    // Static helper methods
    public static void addDroppedItems(@NotNull Game game, @NotNull Collection<Item> items) {
        GameStorage storage = GAMES_STORAGES.computeIfAbsent(game, param -> new GameStorage());
        storage.addDroppedItems(items);
    }

    public static void removeDroppedItems(@NotNull Game game, @NotNull Collection<Item> items) {
        GameStorage storage = GAMES_STORAGES.computeIfAbsent(game, param -> new GameStorage());
        storage.removeDroppedItems(items);
    }

    public static void addDroppedItem(@NotNull Game game, @NotNull Item item) {
        GameStorage storage = GAMES_STORAGES.computeIfAbsent(game, param -> new GameStorage());
        storage.addDroppedItem(item);
    }

    public static void removeDroppedItem(@NotNull Game game, @NotNull Item item) {
        GameStorage storage = GAMES_STORAGES.computeIfAbsent(game, param -> new GameStorage());
        storage.removeDroppedItem(item);
    }

    public static void reset(@NotNull Game game) {
        GameStorage storage = GAMES_STORAGES.get(game);
        if (storage != null)
            storage.removeDroppedItems();
    }

    public static void updateTeam(@NotNull GamePlayer player) {
        GameTeam team = player.getGame().getGameTeam(player.getTeamColor());
        if (team != null)
            team.getUpgradeManager().apply(player);
    }

    public static void checkForSword(@NotNull GamePlayer player) {
        Inventory inv = player.getPlayer().getInventory();

        int wooden_sword_index = -1;
        boolean has_other_sword = false;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null)
                continue;

            if (item.getType() == XBedwarsItems.SWORD.getType()) {
                if (has_other_sword) {
                    inv.setItem(i, null);
                    continue;
                }

                if (wooden_sword_index != -1)
                    inv.setItem(wooden_sword_index, null);

                wooden_sword_index = i;
                continue;
            }

            if (isOtherSwordType(item.getType())) {
                if (wooden_sword_index != -1)
                    inv.setItem(wooden_sword_index, null);

                has_other_sword = true;
            }

        }

        if (!has_other_sword && wooden_sword_index == -1)
            inv.addItem(XBedwarsItems.SWORD);
    }

    private static boolean isOtherSwordType(@NotNull Material type) {
        switch (XMaterial.matchXMaterial(type)) {
            case STONE_SWORD:
            case IRON_SWORD:
            case GOLDEN_SWORD:
            case DIAMOND_SWORD:
            case NETHERITE_SWORD:
                return true;
        }

        return false;
    }

    private static final class GameStorage {

        private final Collection<Item> dropped_items = new HashSet<>();

        public GameStorage() {
        }

        public void addDroppedItems(@NotNull Collection<Item> items) {
            for (Item item : items)
                this.addDroppedItem(item);
        }

        public void removeDroppedItems(@NotNull Collection<Item> items) {
            for (Item item : items)
                this.removeDroppedItem(item);
        }

        public void addDroppedItem(@NotNull Item item) {
            if (dropped_items.add(item))
                item.setMetadata("xbedwars-player-drop", EMPTY_METADATA);
        }

        public void removeDroppedItem(@NotNull Item item) {
            if (dropped_items.remove(item))
                item.removeMetadata("xbedwars-player-drop", XBedwars.getInstance());
        }

        public void removeDroppedItems() {
            for (Item item : dropped_items)
                item.remove();
        }

    }

}