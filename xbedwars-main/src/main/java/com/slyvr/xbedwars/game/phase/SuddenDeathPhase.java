package com.slyvr.xbedwars.game.phase;

import com.slyvr.xbedwars.api.boss.GameBoss;
import com.slyvr.xbedwars.api.boss.GameBossType;
import com.slyvr.xbedwars.api.event.team.GameTeamBossCreationEvent;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.game.phase.GamePhase;
import com.slyvr.xbedwars.api.team.GameTeam;
import com.slyvr.xbedwars.boss.Dragon;
import com.slyvr.xbedwars.upgrade.custom.BossBuffUpgrade;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;


public final class SuddenDeathPhase extends GamePhase {

    public SuddenDeathPhase(int wait) {
        super("Sudden Death", wait);
    }

    @Override
    public boolean apply(@NotNull Game game) {
        if (game == null)
            return false;

        Location loc = game.getArena().getBossSpawnLocation();
        if (loc == null)
            return false;

        for (GameTeam team : game.getGameTeams()) {
            this.spawnDragon(team, loc);

            if (team.getUpgradeManager().contains(BossBuffUpgrade.BOSS_BUFF_UPGRADE))
                this.spawnDragon(team, loc);
        }

        return true;
    }

    private void spawnDragon(@NotNull GameTeam team, @NotNull Location loc) {
        GameTeamBossCreationEvent event = new GameTeamBossCreationEvent(team, Dragon.DRAGON_BOSS_TYPE, loc);
        Bukkit.getPluginManager().callEvent(event);

        GameBoss boss = GameBossType.create(event.getBossType(), team, loc);
        boss.spawn();
    }

}