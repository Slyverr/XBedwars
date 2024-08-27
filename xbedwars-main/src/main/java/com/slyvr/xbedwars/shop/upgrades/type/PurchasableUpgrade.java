package com.slyvr.xbedwars.shop.upgrades.type;

import com.google.common.base.Preconditions;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.lang.NamesRegistry;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.api.shop.content.data.PurchasableCost;
import com.slyvr.xbedwars.api.shop.content.data.PurchasableCosts;
import com.slyvr.xbedwars.api.shop.content.data.PurchasableDescription;
import com.slyvr.xbedwars.api.team.GameTeam;
import com.slyvr.xbedwars.api.team.GameTeamUpgradeManager;
import com.slyvr.xbedwars.api.upgrade.Upgrade;
import com.slyvr.xbedwars.utils.ItemManager;
import com.slyvr.xbedwars.utils.ShopUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class PurchasableUpgrade implements com.slyvr.xbedwars.api.shop.upgrades.type.PurchasableUpgrade {

    private final Upgrade upgrade;
    private final ItemStack display;
    private final PurchasableCosts costs;
    private final PurchasableDescription desc;
    private final NamesRegistry names;

    public PurchasableUpgrade(@NotNull NamesRegistry names, @NotNull ItemStack display, @NotNull Upgrade upgrade, @NotNull PurchasableCosts costs, @NotNull PurchasableDescription desc) {
        Preconditions.checkNotNull(names, "Purchasable's name cannot be null!");
        Preconditions.checkNotNull(display, "Purchasable's display item cannot be null!");
        Preconditions.checkNotNull(upgrade, "Purchasable's upgrade cannot be null!");
        Preconditions.checkNotNull(costs, "Purchasable's costs cannot be null!");
        Preconditions.checkNotNull(desc, "Purchasable's description cannot be null!");

        this.upgrade = upgrade;
        this.names = names;
        this.costs = costs;
        this.desc = desc;

        this.display = display.clone();
    }

    @Override
    public @NotNull NamesRegistry getNames() {
        return names;
    }

    @Override
    public @NotNull Upgrade getUpgrade() {
        return upgrade;
    }

    @Override
    public @NotNull ItemStack getDisplayItem(@NotNull GamePlayer player) {
        Preconditions.checkNotNull(player, "Cannot get the display item for a null player!");

        PurchasableCost cost = costs.getCost(player.getGame().getMode());
        ItemManager manager = new ItemManager(display);

        ShopUtils.setPurchasableDescription(manager, desc, player);
        ShopUtils.setPurchasableFlags(manager);

        ShopUtils.setPurchasableCost(manager, cost, player);
        ShopUtils.setPurchasableSeparator(manager);

        GameTeam team = player.getTeam();
        if (team == null || !team.getUpgradeManager().contains(upgrade))
            return ShopUtils.setPurchasable(manager, names, cost, player).getItemStack();

        ShopUtils.setPurchasableGlowing(manager);
        return ShopUtils.setPurchasableSuccess(manager, names, Message.SHOP_DISPLAY_UNLOCKED, player).getItemStack();
    }

    @Override
    public boolean purchase(@NotNull GamePlayer player) {
        if (player == null)
            return false;

        GameTeam team = player.getTeam();
        if (team == null)
            return false;

        GameTeamUpgradeManager upgrade_manager = team.getUpgradeManager();
        if (upgrade_manager.contains(upgrade)) {
            player.sendMessage(Message.SHOP_PURCHASE_FAILURE_UPGRADE_PURCHASED_BEFORE);
            return false;
        }

        PurchasableCost cost = costs.getCost(player.getGame().getMode());
        if (!ShopUtils.checkCost(cost, player))
            return false;

        if (!upgrade_manager.addUpgrade(upgrade))
            return false;

        if (!ShopUtils.removeCost(cost, player))
            return false;

        upgrade.apply(team);
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