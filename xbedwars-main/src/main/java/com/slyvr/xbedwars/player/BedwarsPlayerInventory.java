package com.slyvr.xbedwars.player;

import com.slyvr.xbedwars.api.player.GamePlayerInventory;
import com.slyvr.xbedwars.api.shop.items.type.TieredPurchasableItem.TieredItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public final class BedwarsPlayerInventory implements GamePlayerInventory {

    private final Map<TieredItemStack, Integer> tiered_items = new HashMap<>();
    private final Set<ItemStack> items = new HashSet<>();

    public BedwarsPlayerInventory() {
    }

    @Override
    public @NotNull Set<ItemStack> getItems() {
        return new HashSet<>(items);
    }

    @Override
    public @NotNull Set<TieredItemStack> getTieredItems() {
        return new HashSet<>(tiered_items.keySet());
    }

    @Override
    public boolean addItem(@NotNull ItemStack item) {
        return item != null && !item.getType().isBlock() && items.add(item);
    }

    @Override
    public boolean removeItem(@NotNull ItemStack item) {
        return item != null && items.remove(item);
    }

    @Override
    public boolean addTieredItem(@NotNull TieredItemStack item) {
        if (item == null)
            return false;

        this.tiered_items.put(item, 1);
        return true;
    }

    @Override
    public int getTieredItemTier(@NotNull TieredItemStack item) {
        return tiered_items.getOrDefault(item, 0);
    }

    @Override
    public void setTieredItemTier(@NotNull TieredItemStack item, int tier) {
        if (item != null && tier >= 1 && tier <= item.getMaximumTier())
            this.tiered_items.put(item, tier);
    }

    @Override
    public boolean removeTieredItem(@NotNull TieredItemStack item) {
        return item != null && tiered_items.remove(item) != null;
    }

    @Override
    public boolean contains(@NotNull Material type) {
        if (type == null)
            return false;

        for (ItemStack item : items) {
            if (item.getType() == type)
                return true;
        }

        for (TieredItemStack tiered_item : tiered_items.keySet()) {
            if (tiered_item.contains(type))
                return true;
        }

        return false;
    }

    @Override
    public boolean contains(@NotNull ItemStack item) {
        if (item == null)
            return false;

        if (items.contains(item))
            return true;

        for (TieredItemStack tiered : tiered_items.keySet()) {
            if (tiered.contains(item))
                return true;
        }

        return false;
    }

    @Override
    public boolean contains(@NotNull TieredItemStack item) {
        return item != null && tiered_items.containsKey(item);
    }

    @Override
    public void sendItems(@NotNull Player player) {
        if (player == null || !player.isOnline())
            return;

        Inventory inv = player.getPlayer().getInventory();

        for (ItemStack item : items)
            inv.addItem(item);

        for (Entry<TieredItemStack, Integer> entry : tiered_items.entrySet()) {
            TieredItemStack item = entry.getKey();
            int tier = entry.getValue();
            if (tier > 1)
                tier--;

            inv.addItem(item.getTier(tier));
            entry.setValue(tier);
        }
    }

    @Override
    public void clear() {
        this.items.clear();
        this.tiered_items.clear();
    }

}