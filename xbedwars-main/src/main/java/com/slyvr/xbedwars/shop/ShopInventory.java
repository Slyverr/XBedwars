package com.slyvr.xbedwars.shop;

import com.google.common.base.Preconditions;
import com.slyvr.xbedwars.api.shop.Shop;
import com.slyvr.xbedwars.api.shop.ShopCategory;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class ShopInventory {

    private static final Map<Inventory, ShopInventory> SHOP_INVENTORIES = new HashMap<>();

    private final Shop<?> shop;
    private final ShopCategory category;
    private final Inventory inventory;

    public ShopInventory(@NotNull Shop<?> shop, @NotNull ShopCategory category, @NotNull String name) {
        Preconditions.checkNotNull(shop, "Inventory's shop cannot be null!");
        Preconditions.checkNotNull(category, "Inventory's category cannot be null!");

        this.shop = shop;
        this.category = category;
        this.inventory = Bukkit.createInventory(null, 54, name);

        ShopInventory.SHOP_INVENTORIES.put(inventory, this);
    }

    @Nullable
    public static ShopInventory getShopInventory(@NotNull Inventory inv) {
        return SHOP_INVENTORIES.get(inv);
    }

    public static boolean isShopInventory(@NotNull Inventory inv) {
        return SHOP_INVENTORIES.containsKey(inv);
    }

    public static boolean removeShopInventory(@NotNull Inventory inv) {
        return inv != null && inv.getViewers().isEmpty() && SHOP_INVENTORIES.remove(inv) != null;
    }

    @NotNull
    public Shop<?> getShop() {
        return shop;
    }

    @NotNull
    public ShopCategory getCategory() {
        return category;
    }

    @NotNull
    public Inventory getInventory() {
        return inventory;
    }

}