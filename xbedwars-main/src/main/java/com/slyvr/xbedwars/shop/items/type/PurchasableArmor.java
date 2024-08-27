package com.slyvr.xbedwars.shop.items.type;

import com.google.common.base.Preconditions;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.lang.NamesRegistry;
import com.slyvr.xbedwars.api.player.ArmorType;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.api.shop.content.SimplePurchasable;
import com.slyvr.xbedwars.api.shop.content.data.PurchasableCost;
import com.slyvr.xbedwars.api.shop.content.data.PurchasableCosts;
import com.slyvr.xbedwars.api.shop.content.data.PurchasableDescription;
import com.slyvr.xbedwars.utils.ItemManager;
import com.slyvr.xbedwars.utils.ShopUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class PurchasableArmor implements SimplePurchasable {


    private final ArmorType type;
    private final ItemStack display;
    private final PurchasableCosts costs;
    private final PurchasableDescription desc;
    private final NamesRegistry names;

    public PurchasableArmor(@NotNull NamesRegistry names, @NotNull ArmorType type, @NotNull PurchasableCosts costs, @NotNull PurchasableDescription desc) {
        Preconditions.checkNotNull(names, "Purchasable's names registry cannot be null!");
        Preconditions.checkNotNull(type, "Purchasable's armor cannot be null!");
        Preconditions.checkNotNull(costs, "Purchasable's costs cannot be null!");
        Preconditions.checkNotNull(desc, "Purchasable's description cannot be null!");

        this.names = names;
        this.costs = costs;
        this.desc = desc;
        this.type = type;

        this.display = new ItemStack(type.getBoots());
    }

    @Override
    public @NotNull NamesRegistry getNames() {
        return names;
    }

    @Override
    public @NotNull ItemStack getDisplayItem(@NotNull GamePlayer player) {
        Preconditions.checkNotNull(player, "Cannot get the display item of a null player!");

        PurchasableCost cost = costs.getCost(player.getGame().getMode());
        ItemManager manager = new ItemManager(display);

        ShopUtils.setPurchasableCost(manager, cost, player);
        ShopUtils.setPurchasableSeparator(manager);

        ShopUtils.setPurchasableDescription(manager, desc, player);
        ShopUtils.setPurchasableQuickBuySetup(manager, this, player);
        ShopUtils.setPurchasableFlags(manager);

        ArmorType armor = player.getArmorType();
        if (armor == type)
            return ShopUtils.setPurchasableSuccess(manager, names, Message.SHOP_DISPLAY_UNLOCKED, player).getItemStack();

        if (armor.ordinal() > type.ordinal())
            return ShopUtils.setPurchasableFailure(manager, names, Message.SHOP_PURCHASE_FAILURE_ARMOR_HIGH_TIER, player).getItemStack();

        return ShopUtils.setPurchasable(manager, names, cost, player).getItemStack();
    }

    @Override
    public boolean purchase(@NotNull GamePlayer player) {
        if (player == null)
            return false;

        ArmorType armor = player.getArmorType();
        if (armor == type) {
            player.sendMessage(Message.SHOP_PURCHASE_FAILURE_ARMOR_EQUIPPED);
            return false;
        }

        if (armor.ordinal() > type.ordinal()) {
            player.sendMessage(Message.SHOP_PURCHASE_FAILURE_ARMOR_HIGH_TIER);
            return false;
        }

        PurchasableCost cost = costs.getCost(player.getGame().getMode());
        if (!ShopUtils.checkCost(cost, player))
            return false;

        if (!ShopUtils.removeCost(cost, player))
            return false;

        player.setArmorType(type);
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