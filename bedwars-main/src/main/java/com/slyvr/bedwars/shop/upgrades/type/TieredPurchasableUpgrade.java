package com.slyvr.bedwars.shop.upgrades.type;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.api.lang.NamesRegistry;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.api.shop.content.data.PurchasableCost;
import com.slyvr.bedwars.api.shop.content.data.PurchasableDescription;
import com.slyvr.bedwars.api.team.GameTeam;
import com.slyvr.bedwars.api.team.GameTeamUpgradeManager;
import com.slyvr.bedwars.api.upgrade.TieredUpgrade;
import com.slyvr.bedwars.utils.ItemManager;
import com.slyvr.bedwars.utils.ShopUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TieredPurchasableUpgrade implements com.slyvr.bedwars.api.shop.upgrades.type.TieredPurchasableUpgrade {

    protected final ItemStack display;
    protected final TieredUpgrade upgrade;
    protected final PurchasableTier[] tiers;
    protected final PurchasableDescription desc;
    protected final NamesRegistry names;

    public TieredPurchasableUpgrade(@NotNull NamesRegistry names, @NotNull ItemStack display, @NotNull TieredUpgrade upgrade, @NotNull List<PurchasableTier> tiers, @NotNull PurchasableDescription desc) {
        Preconditions.checkNotNull(names, "Purchasable's names registry cannot be null!");
        Preconditions.checkNotNull(display, "Purchasable's display-item cannot be null!");
        Preconditions.checkNotNull(upgrade, "Purchasable's tiered-upgrade cannot be null!");

        Preconditions.checkNotNull(tiers, "Purchasable's tiers-list cannot be null!");
        Preconditions.checkArgument(!tiers.isEmpty(), "Purchasable's tiers-list cannot be empty!");
        Preconditions.checkNotNull(desc, "Purchasable's description cannot be null!");

        this.upgrade = upgrade;
        this.names = names;
        this.desc = desc;

        this.tiers = new PurchasableTier[Math.min(upgrade.getMaximumTier(), tiers.size())];

        for (int i = 0; i < Math.min(tiers.size(), upgrade.getMaximumTier()); i++) {
            PurchasableTier tier = tiers.get(i);
            if (tier == null)
                throw new IllegalArgumentException("Purchasable's tier cannot be null!");

            this.tiers[i] = tier;
        }

        this.display = display.clone();
    }

    @Override
    public @NotNull NamesRegistry getNames() {
        return names;
    }

    @Override
    public @NotNull PurchasableDescription getDescription() {
        return desc;
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
        Preconditions.checkNotNull(player, "Cannot get the display item for a null player!");

        GameTeamUpgradeManager upgrade_manager = player.getTeam().getUpgradeManager();
        int current_tier = upgrade_manager.getCurrentTier(upgrade);

        ItemManager manager = new ItemManager(display);

        ShopUtils.setPurchasableDescription(manager, desc, player);
        ShopUtils.setPurchasableFlags(manager);

        ShopUtils.setPurchasableTiers(manager, tiers, current_tier, player);
        ShopUtils.setPurchasableSeparator(manager);

        if (current_tier >= 1)
            ShopUtils.setPurchasableGlowing(manager);

        PurchasableTier tier = tiers[current_tier > 0 ? current_tier - 1 : 0];
        PurchasableCost cost = tier.getCost(player.getGame().getMode());

        if (current_tier == upgrade.getMaximumTier())
            return ShopUtils.setPurchasableFailure(manager, tier.getNames(), Message.SHOP_PURCHASE_FAILURE_TIER_HIGHEST_UNLOCKED, player).getItemStack();
        else
            return ShopUtils.setPurchasable(manager, tier.getNames(), cost, player).getItemStack();
    }

    @Override
    public @NotNull TieredUpgrade getUpgrade() {
        return upgrade;
    }

    @Override
    public boolean purchase(@NotNull GamePlayer player) {
        if (player == null)
            return false;

        GameTeam team = player.getTeam();
        if (team == null)
            return false;

        GameTeamUpgradeManager upgrade_manager = player.getTeam().getUpgradeManager();
        int current_tier = upgrade_manager.getCurrentTier(upgrade);

        if (current_tier == upgrade.getMaximumTier()) {
            player.sendMessage(Message.SHOP_PURCHASE_FAILURE_TIER_HIGHEST_UNLOCKED);
            return false;
        }

        PurchasableTier tier = tiers[current_tier];
        PurchasableCost cost = tier.getCost(player.getGame().getMode());

        if (!ShopUtils.checkCost(cost, player))
            return false;

        if (!ShopUtils.removeCost(cost, player))
            return false;

        upgrade_manager.setCurrentTier(upgrade, current_tier + 1);
        upgrade_manager.apply(player);

        ShopUtils.sendPurchaseMessage(player, tier.getNames());
        return true;
    }

    @Override
    public int size() {
        return tiers.length;
    }

}