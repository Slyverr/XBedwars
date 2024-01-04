package com.slyvr.bedwars.upgrade.custom;

import com.slyvr.bedwars.Bedwars;
import com.slyvr.bedwars.api.arena.team.ArenaTeam;
import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.api.team.GameTeam;
import com.slyvr.bedwars.api.upgrade.Upgrade;
import com.slyvr.bedwars.upgrade.AbstractUpgrade;
import com.slyvr.bedwars.utils.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class HealPoolUpgrade extends AbstractUpgrade {

    public static final Upgrade HEAL_POOL_UPGRADE = new HealPoolUpgrade();

    private final PotionEffect effect;

    private HealPoolUpgrade() {
        super("Heal Pool");

        this.effect = new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0);
    }

    @Override
    public boolean apply(@NotNull GameTeam team) {
        if (team == null)
            return false;

        Game game = team.getGame();
        ArenaTeam arena_team = game.getArena().getTeam(team.getColor());

        Location spawn = arena_team.getSpawnLocation();
        if (spawn == null)
            return false;

        new BukkitRunnable() {

            private final Collection<GamePlayer> players = game.getTeamPlayers(team.getColor());
            private final Region region = getRegionByPoint(spawn);

            @Override
            public void run() {
                if (!game.isRunning() || game.isEliminated(team.getColor())) {
                    this.cancel();
                    return;
                }

                for (GamePlayer team_player : players) {
                    Player bukkit_player = team_player.getPlayer();
                    if (game.isDisconnected(bukkit_player))
                        continue;

                    if (game.isSpectator(bukkit_player))
                        continue;

                    if (!region.isInside(bukkit_player))
                        continue;

                    bukkit_player.addPotionEffect(effect);
                }

            }
        }.runTaskTimer(Bedwars.getInstance(), 0, 20);

        return true;
    }

    private Region getRegionByPoint(@NotNull Location point) {
        Location pos1 = point.clone();
        pos1.add(20, 20, 20);

        Location pos2 = point.clone();
        pos2.subtract(20, 20, 20);

        return new Region(pos1, pos2);
    }

}