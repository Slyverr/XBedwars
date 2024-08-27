package com.slyvr.xbedwars.utils;

import com.cryptomorin.xseries.XSound;
import com.slyvr.xbedwars.api.game.GameMode;
import com.slyvr.xbedwars.api.generator.Resource;
import com.slyvr.xbedwars.api.lang.Language;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.lang.NamesRegistry;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.api.shop.content.Purchasable;
import com.slyvr.xbedwars.api.shop.content.TieredPurchasable.PurchasableTier;
import com.slyvr.xbedwars.api.shop.content.data.PurchasableCost;
import com.slyvr.xbedwars.api.shop.content.data.PurchasableDescription;
import com.slyvr.xbedwars.api.user.User;
import com.slyvr.xbedwars.api.user.shop.UserQuickBuy;
import com.slyvr.xbedwars.manager.BedwarsUsersManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


public final class ShopUtils {

    private static final ItemFlag[] FLAGS = ItemFlag.values();

    private ShopUtils() {
    }

    // Purchasable's display item
    public static void setPurchasableTiers(@NotNull ItemManager manager, @NotNull PurchasableTier[] tiers, int max, @NotNull GamePlayer player) {
        Language lang = MessageUtils.getPlayerLanguage(player.getPlayer());
        GameMode mode = player.getGame().getMode();

        for (int i = 1; i <= tiers.length; i++) {
            PurchasableTier tier = tiers[i - 1];
            String name = tier.getNames().getName(lang);

            if (i > max)
                manager.addLore(lang.format(Message.SHOP_DISPLAY_COST_TIERED, i, name, PurchasableCost.getColoredRepresentingText(tier.getCost(mode), lang)));
            else
                manager.addLore(lang.format(Message.SHOP_DISPLAY_UNLOCKED_TIERED, i, name));
        }

    }

    public static void setPurchasableTier(@NotNull ItemManager manager, int tier, @NotNull GamePlayer player) {
        manager.addLore(MessageUtils.formatLangMessage(Message.SHOP_DISPLAY_TIER, player.getPlayer(), tier));
    }


    public static void setPurchasableCost(@NotNull ItemManager manager, @NotNull PurchasableCost cost, @NotNull GamePlayer player) {
        Language lang = MessageUtils.getPlayerLanguage(player.getPlayer());
        manager.addLore(MessageUtils.formatLangMessage(Message.SHOP_DISPLAY_COST, lang, PurchasableCost.getColoredRepresentingText(cost, lang)));
    }

    public static void setPurchasableDescription(@NotNull ItemManager manager, @NotNull PurchasableDescription desc, @NotNull GamePlayer player) {
        String description = desc.getDescription(MessageUtils.getPlayerLanguage(player.getPlayer()));
        if (description.trim().isEmpty())
            return;

        for (String line : description.split("\\\\n"))
            manager.addLore(ChatColor.GRAY + line);

        manager.addLore("");
    }

    public static void setTieredPurchasableDescription(@NotNull ItemManager manager, @NotNull GamePlayer player) {
        String description = MessageUtils.formatLangMessage(Message.SHOP_DISPLAY_TIERS_DESCRIPTION, player.getPlayer());
        for (String line : description.split("\\n"))
            manager.addLore(ChatColor.GRAY + line);

        manager.addLore("");
    }

    public static void setPurchasableQuickBuySetup(@NotNull ItemManager manager, @NotNull Purchasable purchasable, @NotNull GamePlayer player) {
        User user = BedwarsUsersManager.getInstance().getUser(player.getPlayer());
        if (user == null)
            return;

        UserQuickBuy qb = user.getQuickBuy(player.getGame().getMode());
        if (qb == null || qb.contains(purchasable))
            return;

        manager.addLore(MessageUtils.formatLangMessage(Message.SHOP_DISPLAY_QUICK_BUY_INTERACTION_ADD, user.getLanguage()));
    }

    public static void setPurchasableSeparator(@NotNull ItemManager manager) {
        manager.addLore("");
    }

    public static void setPurchasableGlowing(@NotNull ItemManager manager) {
        manager.addEnchantment(Enchantment.WATER_WORKER, 1, true);
    }

    public static void setPurchasableFlags(@NotNull ItemManager manager) {
        manager.addItemFlags(FLAGS);
    }

    @NotNull
    public static ItemManager setPurchasableSuccess(@NotNull ItemManager manager, @NotNull NamesRegistry names, @NotNull Message message, @NotNull GamePlayer player) {
        Language lang = MessageUtils.getPlayerLanguage(player.getPlayer());
        String name = names.getName(lang);

        manager.addLore(MessageUtils.formatLangMessage(message, lang));
        return !name.trim().isEmpty() ? manager.setDisplayName(ChatColor.GREEN + names.getName(lang)) : manager;
    }

    @NotNull
    public static ItemManager setPurchasableFailure(@NotNull ItemManager manager, @NotNull NamesRegistry names, @NotNull Message message, @NotNull GamePlayer player) {
        Language lang = MessageUtils.getPlayerLanguage(player.getPlayer());
        String name = names.getName(lang);

        manager.addLore(MessageUtils.formatLangMessage(message, lang));
        return !name.trim().isEmpty() ? manager.setDisplayName(ChatColor.RED + names.getName(lang)) : manager;
    }

    @NotNull
    public static ItemManager setPurchasable(@NotNull ItemManager manager, @NotNull NamesRegistry names, @NotNull PurchasableCost cost, @NotNull GamePlayer player) {
        return setPurchasable(manager, names, cost.getResource(), cost.getPrice(), player);
    }

    @NotNull
    public static ItemManager setPurchasable(@NotNull ItemManager manager, @NotNull NamesRegistry names, @NotNull Resource resource, int price, @NotNull GamePlayer player) {
        Language lang = MessageUtils.getPlayerLanguage(player.getPlayer());
        String name = names.getName(lang);

        if (hasEnough(resource, price, player.getPlayer())) {
            manager.addLore(MessageUtils.formatLangMessage(Message.SHOP_DISPLAY_PURCHASE_POSSIBLE, lang));
            return !name.trim().isEmpty() ? manager.setDisplayName(ChatColor.GREEN + names.getName(lang)) : manager;
        }

        manager.addLore(MessageUtils.formatLangMessage(Message.SHOP_DISPLAY_INSUFFICIENT_COST, lang, getResourceName(resource, price, lang)));
        return !name.trim().isEmpty() ? manager.setDisplayName(ChatColor.RED + names.getName(lang)) : manager;

    }

    // Amount Needed
    public static int getAmountNeeded(@NotNull PurchasableCost cost, @NotNull GamePlayer player) {
        return getAmountNeeded(cost.getResource(), cost.getPrice(), player.getPlayer());
    }

    public static int getAmountNeeded(@NotNull PurchasableCost cost, @NotNull Player player) {
        return getAmountNeeded(cost.getResource(), cost.getPrice(), player);
    }

    public static int getAmountNeeded(@NotNull Resource resource, int price, @NotNull GamePlayer player) {
        return getAmountNeeded(resource, price, player.getPlayer());
    }

    public static int getAmountNeeded(@NotNull Resource resource, int price, @NotNull Player player) {
        if (resource.equals(Resource.FREE) || price == 0)
            return 0;

        Material type = resource.getMaterial();
        for (ItemStack item : player.getInventory()) {
            if (item == null || item.getType() != type)
                continue;

            price -= item.getAmount();
            if (price <= 0)
                return 0;
        }

        return price;
    }

    // Purchase Checks
    public static boolean checkItem(@NotNull ItemStack item, @NotNull GamePlayer player) {
        Inventory inv = player.getPlayer().getInventory();
        if (!InventoryUtils.canAddItem(inv, item)) {
            player.sendMessage(Message.PLAYER_INVENTORY_FULL);
            return false;
        }

        return inv.addItem(item).isEmpty();
    }

    // Cost Checks
    public static boolean checkCost(@NotNull PurchasableCost cost, @NotNull GamePlayer player) {
        return checkCost(cost.getResource(), cost.getPrice(), player.getPlayer());
    }

    public static boolean checkCost(@NotNull PurchasableCost cost, @NotNull Player player) {
        return checkCost(cost.getResource(), cost.getPrice(), player);
    }

    public static boolean checkCost(@NotNull Resource resource, int price, @NotNull GamePlayer player) {
        return checkCost(resource, price, player.getPlayer());
    }

    public static boolean checkCost(@NotNull Resource resource, int price, @NotNull Player player) {
        int needed = ShopUtils.getAmountNeeded(resource, price, player);
        if (needed <= 0)
            return true;

        MessageUtils.sendLangMessage(Message.SHOP_PURCHASE_FAILURE_INSUFFICIENT_COST, player, ShopUtils.getResourceName(resource, price, player), needed);
        return false;
    }

    // Cost Removal
    public static boolean removeCost(@NotNull PurchasableCost cost, @NotNull GamePlayer player) {
        return removeCost(cost.getResource(), cost.getPrice(), player.getPlayer());
    }

    public static boolean removeCost(@NotNull PurchasableCost cost, @NotNull Player player) {
        return removeCost(cost.getResource(), cost.getPrice(), player);
    }

    public static boolean removeCost(@NotNull Resource resource, int price, @NotNull GamePlayer player) {
        return removeCost(resource, price, player.getPlayer());
    }

    public static boolean removeCost(@NotNull Resource resource, int price, @NotNull Player player) {
        if (resource.equals(Resource.FREE) || price == 0)
            return true;

        Material type = resource.getMaterial();

        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() != type)
                continue;

            int item_amount = item.getAmount();
            if (price < item_amount) {
                item.setAmount(item_amount - price);
                return true;
            }

            price -= item_amount;
            inv.setItem(i, null);
        }

        return price == 0;
    }

    public static boolean hasEnough(@NotNull PurchasableCost cost, @NotNull GamePlayer player) {
        return ShopUtils.getAmountNeeded(cost, player.getPlayer()) <= 0;
    }

    public static boolean hasEnough(@NotNull PurchasableCost cost, @NotNull Player player) {
        return ShopUtils.getAmountNeeded(cost, player) <= 0;
    }

    public static boolean hasEnough(@NotNull Resource resource, int price, @NotNull GamePlayer player) {
        return ShopUtils.getAmountNeeded(resource, price, player.getPlayer()) <= 0;
    }

    public static boolean hasEnough(@NotNull Resource resource, int price, @NotNull Player player) {
        return ShopUtils.getAmountNeeded(resource, price, player) <= 0;
    }

    // Messages
    public static void sendPurchaseMessage(@NotNull GamePlayer player, @NotNull NamesRegistry names) {
        ShopUtils.sendPurchaseMessage(player.getPlayer(), names);
    }

    public static void sendPurchaseMessage(@NotNull Player player, @NotNull NamesRegistry names) {
        MessageUtils.sendLangMessage(Message.SHOP_PURCHASE_SUCCESS, player, names.getName(MessageUtils.getPlayerLanguage(player)));
        XSound.BLOCK_NOTE_BLOCK_PLING.play(player, 1F, 2F);
    }

    @NotNull
    public static String getResourceName(@NotNull Resource resource, int amount, @NotNull Player player) {
        return getResourceName(resource, amount, MessageUtils.getPlayerLanguage(player));
    }

    @NotNull
    public static String getResourceName(@NotNull Resource resource, int amount, @NotNull Language lang) {
        return amount > 1 ? resource.getPluralName(lang) : resource.getName(lang);
    }

}