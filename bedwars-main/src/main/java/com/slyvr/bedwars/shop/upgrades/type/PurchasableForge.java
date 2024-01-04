package com.slyvr.bedwars.shop.upgrades.type;

import com.slyvr.bedwars.api.generator.Resource;
import com.slyvr.bedwars.api.generator.team.TeamResourceGenerator;
import com.slyvr.bedwars.api.generator.team.TeamResourceGeneratorDrop;
import com.slyvr.bedwars.api.lang.NamesRegistry;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.api.shop.content.data.PurchasableDescription;
import com.slyvr.bedwars.api.team.GameTeam;
import com.slyvr.bedwars.api.team.GameTeamUpgradeManager;
import com.slyvr.bedwars.upgrade.custom.tiered.ForgeUpgrade;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class PurchasableForge extends TieredPurchasableUpgrade {

    public PurchasableForge(@NotNull NamesRegistry names, @NotNull ItemStack display, @NotNull List<PurchasableTier> tiers, @NotNull PurchasableDescription desc) {
        super(names, display, ForgeUpgrade.FORGE_UPGRADE, tiers, desc);
    }

    @Override
    public boolean purchase(@NotNull GamePlayer player) {
        if (!super.purchase(player))
            return false;

        GameTeam team = player.getTeam();
        GameTeamUpgradeManager manager = team.getUpgradeManager();

        TeamResourceGenerator generator = team.getResourceGenerator();
        switch (manager.getCurrentTier(upgrade)) {
            case 1:
            case 2:
                this.increaseDropSpeed(generator, Resource.IRON);
                this.increaseDropSpeed(generator, Resource.GOLD);
                break;
            case 3:
                this.addEmeraldDrop(generator);
                break;
            case 4:
                this.increaseDropSpeed(generator, Resource.IRON);
                this.increaseDropSpeed(generator, Resource.GOLD);
                this.increaseDropSpeed(generator, Resource.EMERALD);
                break;
        }

        return true;
    }

    private void increaseDropSpeed(@NotNull TeamResourceGenerator gen, @NotNull Resource resource) {
        TeamResourceGeneratorDrop drop = gen.getDrop(resource);
        if (drop == null)
            return;

        drop.setDropsPerMinute(drop.getDropsPerMinute() + (int) (drop.getDropsPerMinute() * 0.5F));
        gen.updateDrop(resource);
    }

    private void addEmeraldDrop(@NotNull TeamResourceGenerator gen) {
        if (!gen.hasDrop(Resource.EMERALD))
            gen.addDrop(new TeamResourceGeneratorDrop(Resource.EMERALD, 1, 2));
    }

}