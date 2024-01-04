package com.slyvr.bedwars.shop.upgrades;

import com.slyvr.bedwars.api.lang.NamesRegistry;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.api.shop.content.Purchasable;
import com.slyvr.bedwars.api.shop.upgrades.UpgradeShop.UpgradeShopCategory;
import com.slyvr.bedwars.shop.AbstractShopCategory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.Map.Entry;

public final class BedwarsUpgradeShopCategory extends AbstractShopCategory implements UpgradeShopCategory {

    public BedwarsUpgradeShopCategory(@NotNull NamesRegistry names, @NotNull ItemStack icon, @Nullable Map<Integer, Purchasable> content) {
        super(names, icon, content);
    }

    public BedwarsUpgradeShopCategory(@NotNull NamesRegistry names, @NotNull ItemStack icon) {
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

        if (index >= 1 && index <= 7)
            return index - 1;

        if (index >= 10 && index <= 16)
            return index - 3;

        if (index >= 19 && index <= 25)
            return index - 5;

        return -1;
    }

    private @Range(from = -1, to = 53) int toInventorySlot(@Range(from = 0, to = 20) int index) {
        if (index >= 0 && index <= 6)
            return index + 1;

        if (index >= 7 && index <= 13)
            return index + 3;

        if (index >= 14 && index <= 20)
            return index + 5;

        return -1;
    }

}