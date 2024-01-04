package com.slyvr.bedwars.shop.items.type;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.api.lang.NamesRegistry;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.api.player.GamePlayerInventory;
import com.slyvr.bedwars.api.shop.content.data.PurchasableCost;
import com.slyvr.bedwars.api.shop.content.data.PurchasableCosts;
import com.slyvr.bedwars.api.shop.content.data.PurchasableDescription;
import com.slyvr.bedwars.api.shop.items.type.PurchasableItem;
import com.slyvr.bedwars.team.BedwarsTeamUtils;
import com.slyvr.bedwars.utils.ItemManager;
import com.slyvr.bedwars.utils.ShopUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class PurchasableItemStack implements PurchasableItem {

    private final ItemStack item;
    private final ItemStack display;
    private final PurchasableCosts costs;
    private final PurchasableDescription desc;
    private final NamesRegistry names;
    private final boolean permanent;

    public PurchasableItemStack(@NotNull NamesRegistry names, @NotNull ItemStack item, @NotNull PurchasableCosts costs, @NotNull PurchasableDescription desc, boolean permanent) {
        Preconditions.checkNotNull(names, "Purchasable's name cannot be null!");
        Preconditions.checkNotNull(item, "Purchasable's item cannot be null!");
        Preconditions.checkNotNull(costs, "Purchasable's costs cannot be null!");
        Preconditions.checkNotNull(desc, "Purchasable's description cannot be null!");

        this.names = names;
        this.costs = costs;
        this.desc = desc;

        this.item = item;
        this.display = item.clone();

        this.permanent = permanent && !item.getType().isBlock();
    }


    @Override
    public @NotNull NamesRegistry getNames() {
        return names;
    }

    @Override
    public @NotNull ItemStack getDisplayItem(@NotNull GamePlayer player) {
        if (player == null)
            return null;

        PurchasableCost cost = costs.getCost(player.getGame().getMode());
        ItemManager manager = new ItemManager(display);

        ShopUtils.setPurchasableCost(manager, cost, player);
        ShopUtils.setPurchasableSeparator(manager);

        ShopUtils.setPurchasableDescription(manager, desc, player);
        ShopUtils.setPurchasableQuickBuySetup(manager, this, player);
        ShopUtils.setPurchasableFlags(manager);

        if (permanent && player.getInventory().contains(item))
            return ShopUtils.setPurchasableFailure(manager, names, Message.SHOP_PURCHASE_FAILURE_ITEM_OWNED, player).getItemStack();
        else
            return ShopUtils.setPurchasable(manager, names, cost, player).getItemStack();
    }

    @Override
    public @NotNull ItemStack getItem() {
        return item.clone();
    }

    @Override
    public boolean purchase(@NotNull GamePlayer player) {
        if (player == null)
            return false;

        GamePlayerInventory inventory = player.getInventory();
        if (permanent && inventory.contains(item.getType())) {
            player.sendMessage(Message.SHOP_PURCHASE_FAILURE_ITEM_PURCHASED_BEFORE);
            return false;
        }

        PurchasableCost cost = costs.getCost(player.getGame().getMode());
        if (!ShopUtils.checkCost(cost, player))
            return false;

        ItemStack normal = item.clone();
        if (permanent && !player.getInventory().addItem(normal))
            return false;

        ItemStack colored = BedwarsTeamUtils.getColoredBlock(normal, player.getTeamColor());
        if (!ShopUtils.checkItem(colored, player))
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