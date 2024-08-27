package com.slyvr.xbedwars.upgrade.custom;

import com.slyvr.xbedwars.api.team.GameTeam;
import com.slyvr.xbedwars.upgrade.AbstractUpgrade;
import org.jetbrains.annotations.NotNull;

public final class BossBuffUpgrade extends AbstractUpgrade {

    public static final BossBuffUpgrade BOSS_BUFF_UPGRADE = new BossBuffUpgrade();

    private BossBuffUpgrade() {
        super("Boss Buff");
    }

    @Override
    public boolean apply(@NotNull GameTeam team) {
        return team != null;
    }

}