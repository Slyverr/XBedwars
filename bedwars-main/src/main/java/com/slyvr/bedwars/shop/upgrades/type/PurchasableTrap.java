package com.slyvr.bedwars.shop.upgrades.type;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.game.GameMode;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.api.lang.NamesRegistry;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.api.shop.content.data.PurchasableCost;
import com.slyvr.bedwars.api.shop.content.data.PurchasableDescription;
import com.slyvr.bedwars.api.team.GameTeamTrapManager;
import com.slyvr.bedwars.api.trap.Trap;
import com.slyvr.bedwars.configuration.shop.UpgradeShopConfig;
import com.slyvr.bedwars.utils.ItemManager;
import com.slyvr.bedwars.utils.ShopUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class PurchasableTrap implements com.slyvr.bedwars.api.shop.upgrades.type.PurchasableTrap {

    private final Trap trap;
    private final ItemStack display;
    private final PurchasableDescription desc;
    private final NamesRegistry names;

    public PurchasableTrap(@NotNull NamesRegistry names, @NotNull ItemStack display, @NotNull Trap trap, @NotNull PurchasableDescription desc) {
        Preconditions.checkNotNull(names, "Purchasable's names cannot be null!");
        Preconditions.checkNotNull(display, "Purchasable's display item cannot be null!");
        Preconditions.checkNotNull(trap, "Purchasable's trap cannot be null!");
        Preconditions.checkNotNull(desc, "Purchasable's description cannot be null!");

        this.names = names;
        this.trap = trap;
        this.desc = desc;
        this.display = display;
    }

    @Override
    public @NotNull NamesRegistry getNames() {
        return names;
    }

    @Override
    public @NotNull Trap getTrap() {
        return trap;
    }


    @Override
    public @NotNull ItemStack getDisplayItem(@NotNull GamePlayer player) {
        Preconditions.checkNotNull(player, "Cannot get the display item for a null player!");

        GameTeamTrapManager trap_manager = player.getTeam().getTrapManager();
        ItemManager manager = new ItemManager(display);

        GameMode mode = player.getGame().getMode();
        PurchasableCost cost = UpgradeShopConfig.getInstance().getTrapCost(mode, trap_manager.size());

        ShopUtils.setPurchasableDescription(manager, desc, player);
        ShopUtils.setPurchasableFlags(manager);

        if (trap_manager.contains(trap))
            ShopUtils.setPurchasableGlowing(manager);

        if (trap_manager.size() >= UpgradeShopConfig.getInstance().getTrapsLimit(mode))
            return ShopUtils.setPurchasableFailure(manager, names, Message.SHOP_PURCHASE_FAILURE_TRAP_LIMIT_REACHED, player).getItemStack();

        ShopUtils.setPurchasableCost(manager, cost, player);
        ShopUtils.setPurchasableSeparator(manager);

        return ShopUtils.setPurchasable(manager, names, cost, player).getItemStack();
    }

    @Override
    public boolean purchase(@NotNull GamePlayer player) {
        if (player == null)
            return false;

        GameTeamTrapManager trap_manager = player.getTeam().getTrapManager();
        GameMode mode = player.getGame().getMode();

        if (trap_manager.size() >= UpgradeShopConfig.getInstance().getTrapsLimit(mode)) {
            player.sendMessage(Message.SHOP_PURCHASE_FAILURE_TRAP_LIMIT_REACHED);
            return false;
        }

        PurchasableCost cost = UpgradeShopConfig.getInstance().getTrapCost(mode, trap_manager.size());
        if (!ShopUtils.checkCost(cost, player))
            return false;

        if (!trap_manager.addTrap(trap))
            return false;

        if (!ShopUtils.removeCost(cost, player))
            return false;

        ShopUtils.sendPurchaseMessage(player, names);
        return true;
    }

    @Override
    public @NotNull PurchasableCost getCost(@NotNull GamePlayer player) {
        return UpgradeShopConfig.getInstance().getTrapCost(player.getGame().getMode(), player.getTeam().getTrapManager().size());
    }

    @Override
    public @NotNull PurchasableDescription getDescription() {
        return desc;
    }
}