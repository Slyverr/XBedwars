package com.slyvr.bedwars.shop.items.type;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.api.lang.NamesRegistry;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.api.player.GamePlayerInventory;
import com.slyvr.bedwars.api.shop.content.data.PurchasableCost;
import com.slyvr.bedwars.api.shop.content.data.PurchasableDescription;
import com.slyvr.bedwars.api.shop.items.type.TieredPurchasableItem;
import com.slyvr.bedwars.utils.ItemManager;
import com.slyvr.bedwars.utils.ShopUtils;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TieredPurchasableItemStack implements TieredPurchasableItem {


    private final TieredItemStack item;
    private final PurchasableTier[] tiers;
    private final PurchasableDescription desc;
    private final NamesRegistry names;

    public TieredPurchasableItemStack(@NotNull NamesRegistry names, @NotNull TieredItemStack item, @NotNull List<PurchasableTier> tiers, @NotNull PurchasableDescription desc) {
        Preconditions.checkNotNull(names, "Purchasable's names registry cannot be null!");
        Preconditions.checkNotNull(item, "Purchasable's tiered item cannot be null!");
        Preconditions.checkNotNull(tiers, "Purchasable's tiers list cannot be null!");
        Preconditions.checkArgument(!tiers.isEmpty(), "Purchasable's tiers-list cannot be empty!");
        Preconditions.checkNotNull(desc, "Purchasable's description cannot be null!");

        this.names = names;
        this.item = item;
        this.desc = desc;

        this.tiers = new PurchasableTier[Math.min(item.getMaximumTier(), tiers.size())];
        for (int i = 0; i < tiers.size() && i < item.getMaximumTier(); i++) {
            PurchasableTier tier = tiers.get(i);
            if (tier == null)
                throw new IllegalArgumentException("Purchasable's tier cannot be null!");

            this.tiers[i] = tier;
        }

    }


    @Override
    public @NotNull NamesRegistry getNames() {
        return names;
    }

    @Override
    public @NotNull List<PurchasableTier> getTiers() {
        List<PurchasableTier> result = new ArrayList<>(tiers.length);
        Collections.addAll(result, tiers);

        return result;
    }

    @Override
    public @Nullable PurchasableTier getTier(int tier) {
        return tier >= 1 && tier <= tiers.length ? tiers[tier - 1] : null;
    }


    @Override
    public @NotNull ItemStack getDisplayItem(@NotNull GamePlayer player) {
        Preconditions.checkNotNull(player, "Cannot get the display item of a null player!");

        int current = player.getInventory().getTieredItemTier(item);
        int next = Math.min(current + 1, item.getMaximumTier());

        ItemManager manager = new ItemManager(item.getTier(next));

        PurchasableTier tier = tiers[next - 1];
        PurchasableCost cost = tier.getCost(player.getGame().getMode());

        ShopUtils.setPurchasableTier(manager, next, player);
        ShopUtils.setPurchasableCost(manager, cost, player);
        ShopUtils.setPurchasableSeparator(manager);

        ShopUtils.setTieredPurchasableDescription(manager, player);
        ShopUtils.setPurchasableDescription(manager, desc, player);
        ShopUtils.setPurchasableFlags(manager);

        ShopUtils.setPurchasableQuickBuySetup(manager, this, player);
        if (current == item.getMaximumTier())
            return ShopUtils.setPurchasableFailure(manager, tier.getNames(), Message.SHOP_PURCHASE_FAILURE_TIER_HIGHEST_UNLOCKED, player).getItemStack();
        else
            return ShopUtils.setPurchasable(manager, tier.getNames(), cost, player).getItemStack();
    }

    @Override
    public @NotNull TieredItemStack getItem() {
        return item;
    }

    @Override
    public boolean purchase(@NotNull GamePlayer player) {
        if (player == null)
            return false;

        int current_tier = player.getInventory().getTieredItemTier(item);
        if (current_tier == item.getMaximumTier()) {
            player.sendMessage(Message.SHOP_PURCHASE_FAILURE_TIER_HIGHEST_UNLOCKED);
            return false;
        }

        PurchasableTier tier = tiers[current_tier];
        PurchasableCost cost = tier.getCost(player.getGame().getMode());

        if (!ShopUtils.checkCost(cost, player))
            return false;

        ItemStack current = item.getTier(current_tier);
        ItemStack next = item.getTier(current_tier + 1);

        Inventory player_inv = player.getPlayer().getInventory();
        player_inv.remove(current);

        if (!ShopUtils.checkItem(next, player))
            return false;

        if (!ShopUtils.removeCost(cost, player))
            return false;

        GamePlayerInventory inventory = player.getInventory();
        inventory.setTieredItemTier(item, current_tier + 1);

        ShopUtils.sendPurchaseMessage(player, tier.getNames());
        return true;
    }

    @Override
    public @NotNull PurchasableDescription getDescription() {
        return desc;
    }

    @Override
    public int size() {
        return tiers.length;
    }

}