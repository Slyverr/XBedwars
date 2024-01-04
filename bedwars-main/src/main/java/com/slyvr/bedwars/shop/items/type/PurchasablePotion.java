package com.slyvr.bedwars.shop.items.type;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.lang.NamesRegistry;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.api.shop.content.data.PurchasableCost;
import com.slyvr.bedwars.api.shop.content.data.PurchasableCosts;
import com.slyvr.bedwars.api.shop.content.data.PurchasableDescription;
import com.slyvr.bedwars.api.shop.items.type.PurchasableItem;
import com.slyvr.bedwars.utils.ItemManager;
import com.slyvr.bedwars.utils.ShopUtils;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

public final class PurchasablePotion implements PurchasableItem {

    private static final ItemFlag[] FLAGS = ItemFlag.values();

    private final ItemStack potion;
    private final PurchasableCosts costs;
    private final PurchasableDescription desc;
    private final NamesRegistry names;

    public PurchasablePotion(@NotNull NamesRegistry names, @NotNull PotionEffect effect, @NotNull PurchasableCosts costs, @NotNull PurchasableDescription desc) {
        Preconditions.checkNotNull(names, "Purchasable's names registry cannot be null!");
        Preconditions.checkNotNull(effect, "Purchasable's potion-effect cannot be null!");
        Preconditions.checkNotNull(costs, "Purchasable's costs cannot be null!");
        Preconditions.checkNotNull(desc, "Purchasable's description cannot be null!");

        this.names = names;
        this.costs = costs;
        this.desc = desc;

        this.potion = getPotionItem(effect);
    }

    @NotNull
    private ItemStack getPotionItem(@NotNull PotionEffect effect) {
        ItemStack potion = new Potion(PotionType.getByEffect(effect.getType())).toItemStack(1);

        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.clearCustomEffects();

        meta.addCustomEffect(effect, true);
        meta.addItemFlags(FLAGS);

        potion.setItemMeta(meta);
        return potion;
    }

    @Override
    public @NotNull NamesRegistry getNames() {
        return names;
    }

    @Override
    public @NotNull ItemStack getDisplayItem(@NotNull GamePlayer player) {
        Preconditions.checkNotNull(player, "Cannot get the display name of a null player!");

        PurchasableCost cost = costs.getCost(player.getGame().getMode());
        ItemManager manager = new ItemManager(potion);

        ShopUtils.setPurchasableCost(manager, cost, player);
        ShopUtils.setPurchasableSeparator(manager);

        ShopUtils.setPurchasableDescription(manager, desc, player);
        ShopUtils.setPurchasableQuickBuySetup(manager, this, player);

        return ShopUtils.setPurchasable(manager, names, cost, player).getItemStack();
    }

    @Override
    public @NotNull ItemStack getItem() {
        return potion.clone();
    }

    @Override
    public boolean purchase(@NotNull GamePlayer player) {
        if (player == null)
            return false;

        PurchasableCost cost = costs.getCost(player.getGame().getMode());
        if (!ShopUtils.checkCost(cost, player))
            return false;

        if (!ShopUtils.checkItem(potion.clone(), player))
            return false;

        if (!ShopUtils.removeCost(cost, player))
            return false;

        ShopUtils.sendPurchaseMessage(player, names);
        return true;
    }

    @Override
    public @NotNull PurchasableCost getCost(@NotNull GamePlayer player) {
        return costs.getCost(player.getGame().getMode());
    }

    @Override
    public @NotNull PurchasableDescription getDescription() {
        return desc;
    }

}