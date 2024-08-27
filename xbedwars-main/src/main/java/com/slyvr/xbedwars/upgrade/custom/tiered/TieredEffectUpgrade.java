package com.slyvr.xbedwars.upgrade.custom.tiered;

import com.google.common.base.Preconditions;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.api.team.GameTeam;
import com.slyvr.xbedwars.api.upgrade.TieredUpgrade;
import com.slyvr.xbedwars.upgrade.AbstractTieredUpgrade;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class TieredEffectUpgrade extends AbstractTieredUpgrade {

    public static final TieredUpgrade MANIAC_MINER_UPGRADE;

    static {
        PotionEffect haste1 = new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 0);
        PotionEffect haste2 = new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 1);

        MANIAC_MINER_UPGRADE = new TieredEffectUpgrade("Maniac Miner", Arrays.asList(haste1, haste2));
    }

    private final List<PotionEffect> effects;

    public TieredEffectUpgrade(@NotNull String name, @NotNull List<PotionEffect> effects) {
        super(name, Preconditions.checkNotNull(effects, "TieredUpgrade's effects list cannot be null!").size());

        List<PotionEffect> result = new ArrayList<>(effects.size());
        for (PotionEffect effect : effects) {
            if (effect == null)
                throw new IllegalArgumentException("TieredUpgrade's effect cannot be null!");

            result.add(effect);
        }

        this.effects = result;
    }

    @Override
    public boolean apply(@NotNull GameTeam team) {
        if (team == null)
            return false;

        Game game = team.getGame();

        Collection<GamePlayer> team_players = game.getTeamPlayers(team.getColor());
        if (team_players.isEmpty())
            return false;

        int current_tier = team.getUpgradeManager().getCurrentTier(this);
        if (current_tier == 0)
            return false;

        PotionEffect effect = effects.get(current_tier - 1);
        for (GamePlayer team_player : team_players) {
            Player bukkit_player = team_player.getPlayer();

            if (game.isEliminated(bukkit_player) || game.isSpectator(bukkit_player))
                continue;

            bukkit_player.addPotionEffect(effect);
        }

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof TieredEffectUpgrade))
            return false;

        if (!super.equals(obj))
            return false;

        TieredEffectUpgrade other = (TieredEffectUpgrade) obj;
        return effects.equals(other.effects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), effects);
    }

}