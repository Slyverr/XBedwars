package com.slyvr.xbedwars.trap;

import com.slyvr.xbedwars.api.event.player.trap.GamePlayerTrapTriggerEvent;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.api.team.TeamColor;
import com.slyvr.xbedwars.api.trap.TrapTarget;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public final class CounterOffensiveTrap extends AbstractTrap {

    private static final PotionEffect SPEED = new PotionEffect(PotionEffectType.SPEED, 15 * 20, 1);
    private static final PotionEffect JUMP = new PotionEffect(PotionEffectType.JUMP, 15 * 20, 1);

    public CounterOffensiveTrap(int duration) {
        super("Counter Offensive Trap", TrapTarget.TRAP_TEAM, duration);
    }

    public CounterOffensiveTrap() {
        this(10);
    }

    @Override
    public boolean onTrigger(@NotNull GamePlayer player, @NotNull TeamColor color) {
        if (player == null || color == null || player.isTrapSafe())
            return false;

        GamePlayerTrapTriggerEvent event = new GamePlayerTrapTriggerEvent(player, this, color);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return false;

        Game game = player.getGame();
        Location team_spawn = game.getArena().getTeam(player.getTeamColor()).getSpawnLocation();
        if (team_spawn == null)
            return false;

        for (GamePlayer team_player : game.getTeamPlayers(color)) {
            Player bukkit_player = team_player.getPlayer();
            if (game.isDisconnected(bukkit_player))
                continue;

            if (bukkit_player.getLocation().distanceSquared(team_spawn) >= 400) // Checks if the player is inside a certain radius (20 blocks)
                continue;

            bukkit_player.addPotionEffect(SPEED);
            bukkit_player.addPotionEffect(JUMP);
        }

        return true;
    }

}