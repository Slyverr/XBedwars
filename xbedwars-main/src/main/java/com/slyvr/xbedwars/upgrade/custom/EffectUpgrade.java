package com.slyvr.xbedwars.upgrade.custom;

import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.api.team.GameTeam;
import com.slyvr.xbedwars.upgrade.AbstractUpgrade;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class EffectUpgrade extends AbstractUpgrade {

    private final PotionEffect effect;

    public EffectUpgrade(@NotNull String name, @NotNull PotionEffect effect) {
        super(name);

        this.effect = effect;
    }

    @Override
    public boolean apply(@NotNull GameTeam team) {
        if (team == null)
            return false;

        Game game = team.getGame();
        Collection<GamePlayer> team_players = game.getTeamPlayers(team.getColor());
        if (team_players.isEmpty())
            return false;

        for (GamePlayer team_player : team_players) {
            Player bukkit_player = team_player.getPlayer();
            if (game.isEliminated(bukkit_player))
                continue;

            if (game.isSpectator(bukkit_player))
                continue;

            bukkit_player.addPotionEffect(effect);
        }

        return true;
    }

}