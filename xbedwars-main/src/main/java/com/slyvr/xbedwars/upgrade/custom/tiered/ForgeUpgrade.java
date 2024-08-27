package com.slyvr.xbedwars.upgrade.custom.tiered;

import com.slyvr.xbedwars.api.team.GameTeam;
import com.slyvr.xbedwars.api.upgrade.TieredUpgrade;
import com.slyvr.xbedwars.upgrade.AbstractTieredUpgrade;
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