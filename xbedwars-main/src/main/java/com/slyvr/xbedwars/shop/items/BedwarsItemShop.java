package com.slyvr.xbedwars.shop.items;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Preconditions;
import com.slyvr.xbedwars.api.event.shop.ShopOpenEvent;
import com.slyvr.xbedwars.api.event.user.shop.UserQuickBuyOpenEvent;
import com.slyvr.xbedwars.api.lang.Language;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.api.shop.ShopCategory;
import com.slyvr.xbedwars.api.shop.items.ItemShop;
import com.slyvr.xbedwars.api.user.shop.UserQuickBuy;
import com.slyvr.xbedwars.manager.BedwarsUsersManager;
import com.slyvr.xbedwars.shop.ShopInventory;
import com.slyvr.xbedwars.user.shop.BedwarsUserQuickBuy;
import com.slyvr.xbedwars.utils.ItemManager;
import com.slyvr.xbedwars.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class BedwarsItemShop implements ItemShop {

    private static final Map<Language, ItemStack> GREEN_SEPARATORS;
    private static final Map<Language, ItemStack> GRAY_SEPARATORS;

    static {
        Map<Language, ItemStack> green_separators = new HashMap<>();
        Map<Language, ItemStack> gray_separators = new HashMap<>();

        for (Language lang : Language.values()) {
            ItemManager separator = new ItemManager(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem());
            separator.setDisplayName(MessageUtils.formatLangMessage(Message.SHOP_DISPLAY_SEPARATOR_CATEGORIES, lang));
            separator.addLore(MessageUtils.formatLangMessage(Message.SHOP_DISPLAY_SEPARATOR_ITEMS, lang));

            gray_separators.put(lang, separator.getItemStack());
            green_separators.put(lang, XMaterial.GREEN_STAINED_GLASS_PANE.setType(separator.getItemStack().clone()));
        }

        GREEN_SEPARATORS = green_separators;
        GRAY_SEPARATORS = gray_separators;
    }

    private final Map<Integer, ItemShopCategory> categories;

    public BedwarsItemShop(@NotNull List<ItemShopCategory> categories) {
        Preconditions.checkNotNull(categories, "Shop's categories list cannot be null!");

        Map<Integer, ItemShopCategory> categories_map = new HashMap<>(8, 1F);

        for (int i = 0; i < Math.min(categories.size(), 8); i++) {
            ItemShopCategory category = categories.get(i);
            if (category == null)
                throw new IllegalArgumentException("Shop's category cannot be null!");

            if (category instanceof UserQuickBuy)
                throw new IllegalArgumentException("Shop's category cannot be quick-buy!");

            categories_map.put(i, category);
        }

        this.categories = Collections.unmodifiableMap(categories_map);
    }

    @Override
    public @NotNull Map<Integer, ItemShopCategory> getCategories() {
        return categories;
    }

    @Override
    public @Nullable ItemShopCategory getCategory(int index) {
        return categories.get(index);
    }

    @Override
    public @NotNull ShopType getType() {
        return ShopType.ITEMS;
    }

    @Override
    public boolean click(@NotNull GamePlayer player, @Range(from = 0, to = 8) int slot) {
        if (player == null || slot < 0 || slot > 8)
            return false;

        if (slot == 0)
            return open(player);

        ShopCategory category = categories.get(slot - 1);
        if (category == null)
            return false;

        ShopOpenEvent event = new ShopOpenEvent(this, category, player);
        Bukkit.getPluginManager().callEvent(event);

        return !event.isCancelled() && open(player, category, slot);
    }

    @Override
    public boolean open(@NotNull GamePlayer player, @Nullable ShopCategory category) {
        if (category == null)
            return open(player);

        if (player == null)
            return false;

        int category_index = -1;
        for (Entry<Integer, ItemShopCategory> entry : categories.entrySet()) {
            if (!entry.getValue().equals(category))
                continue;

            category_index = entry.getKey();
            break;
        }

        if (category_index == -1)
            return false;

        ShopOpenEvent event = new ShopOpenEvent(this, category, player);
        Bukkit.getPluginManager().callEvent(event);

        return !event.isCancelled() && open(player, category, category_index + 1);
    }

    @Override
    public boolean open(@NotNull GamePlayer player) {
        if (player == null)
            return false;

        UserQuickBuy user_category = getUserQuickBuy(player);
        if (user_category == null)
            return false;

        UserQuickBuyOpenEvent event = new UserQuickBuyOpenEvent(user_category);
        Bukkit.getPluginManager().callEvent(event);

        return !event.isCancelled() && open(player, user_category, 0);
    }

    private boolean open(@NotNull GamePlayer player, @NotNull ShopCategory category, @Range(from = 0, to = 8) int index) {
        Language language = MessageUtils.getPlayerLanguage(player.getPlayer());

        ShopInventory shop_inv = new ShopInventory(this, category, category.getNames().getName(language));
        Inventory inv = shop_inv.getInventory();

        inv.setItem(0, BedwarsUserQuickBuy.getDisplayItem(language));
        inv.setItem(9, index == 0 ? GREEN_SEPARATORS.get(language) : GRAY_SEPARATORS.get(language));

        for (int i = 1; i < 9; i++) {
            ItemShopCategory current = categories.get(i - 1);
            if (current == null)
                continue;

            inv.setItem(i, current.getDisplayItem(player));
            inv.setItem(i + 9, i == index ? GREEN_SEPARATORS.get(language) : GRAY_SEPARATORS.get(language));
        }

        category.populate(inv, player);
        return player.getPlayer().openInventory(inv) != null;
    }

    @Override
    public boolean contains(@NotNull ItemShopCategory category) {
        return category != null && categories.containsValue(category);
    }

    @NotNull
    private UserQuickBuy getUserQuickBuy(@NotNull GamePlayer player) {
        return BedwarsUsersManager.getInstance().getOfflineUser(player.getPlayer()).getQuickBuy(player.getGame().getMode());
    }

}