package com.slyvr.bedwars.game.phase;

import com.slyvr.bedwars.api.boss.GameBoss;
import com.slyvr.bedwars.api.boss.GameBossType;
import com.slyvr.bedwars.api.event.team.GameTeamBossCreationEvent;
import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.game.phase.GamePhase;
import com.slyvr.bedwars.api.team.GameTeam;
import com.slyvr.bedwars.boss.Dragon;
import com.slyvr.bedwars.upgrade.custom.BossBuffUpgrade;
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