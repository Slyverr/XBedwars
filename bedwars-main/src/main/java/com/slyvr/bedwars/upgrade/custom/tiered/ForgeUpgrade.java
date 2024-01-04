package com.slyvr.bedwars.upgrade.custom.tiered;

import com.slyvr.bedwars.api.team.GameTeam;
import com.slyvr.bedwars.api.upgrade.TieredUpgrade;
import com.slyvr.bedwars.upgrade.AbstractTieredUpgrade;
import org.jetbrains.annotations.NotNull;

public final class ForgeUpgrade extends AbstractTieredUpgrade {

    public static final TieredUpgrade FORGE_UPGRADE = new ForgeUpgrade();

    private ForgeUpgrade() {
        super("Forge Upgrade", 4);
    }

    @Override
    public boolean apply(@NotNull GameTeam team) {
        return team != null;
    }

}