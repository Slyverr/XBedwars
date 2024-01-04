package com.slyvr.bedwars.upgrade.custom;

import com.slyvr.bedwars.api.team.GameTeam;
import com.slyvr.bedwars.upgrade.AbstractUpgrade;
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