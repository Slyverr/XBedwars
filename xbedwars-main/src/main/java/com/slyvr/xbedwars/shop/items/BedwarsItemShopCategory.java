package com.slyvr.xbedwars.shop.items;

import com.slyvr.xbedwars.api.lang.NamesRegistry;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.api.shop.content.Purchasable;
import com.slyvr.xbedwars.api.shop.items.ItemShop.ItemShopCategory;
import com.slyvr.xbedwars.shop.AbstractShopCategory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.Map.Entry;

public final class BedwarsItemShopCategory extends AbstractShopCategory implements ItemShopCategory {

    public BedwarsItemShopCategory(@NotNull NamesRegistry names, @NotNull ItemStack icon, @Nullable Map<Integer, Purchasable> content) {
        super(names, icon, content);
    }


    public BedwarsItemShopCategory(@NotNull NamesRegistry names, @NotNull ItemStack icon) {
        super(names, icon);
    }

    @Override
    public boolean populate(@NotNull Inventory inventory, @NotNull GamePlayer player) {
        if (inventory == null || player == null)
            return false;

        for (Entry<Integer, Purchasable> entry : content.entrySet())
            inventory.setItem(toInventorySlot(entry.getKey()), entry.getValue().getDisplayItem(player));

        return true;
    }

    @Override
    public boolean click(@NotNull GamePlayer player, @Range(from = 0, to = 53) int slot) {
        if (player == null)
            return false;

        Purchasable purchasable = content.get(toCategorySlot(slot));
        return purchasable != null && purchasable.purchase(player);
    }

    public @Range(from = -1, to = 20) int toCategorySlot(@Range(from = 0, to = 53) int index) {
        if (index < 0 || index > 53)
            return -1;

        if (index >= 19 && index <= 25)
            return index - 19;

        if (index >= 28 && index <= 34)
            return index - 21;

        if (index >= 37 && index <= 43)
            return index - 23;

        return -1;
    }

    private @Range(from = -1, to = 53) int toInventorySlot(@Range(from = 0, to = 20) int index) {
        if (index >= 0 && index <= 6)
            return index + 19;

        if (index >= 7 && index <= 13)
            return index + 21;

        if (index >= 14 && index <= 20)
            return index + 23;

        return -1;
    }

}