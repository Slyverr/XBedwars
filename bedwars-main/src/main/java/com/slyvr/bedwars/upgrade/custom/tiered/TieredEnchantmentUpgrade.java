package com.slyvr.bedwars.upgrade.custom.tiered;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.api.team.GameTeam;
import com.slyvr.bedwars.api.upgrade.TieredUpgrade;
import com.slyvr.bedwars.upgrade.AbstractTieredUpgrade;
import com.slyvr.bedwars.utils.EnchantmentUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public final class TieredEnchantmentUpgrade extends AbstractTieredUpgrade {

    public static final TieredUpgrade PROTECTION_UPGRADE = new TieredEnchantmentUpgrade("Protection", EnchantmentTarget.ARMOR, Enchantment.PROTECTION_ENVIRONMENTAL);

    private final EnchantmentTarget target;
    private final Enchantment ench;
    private final boolean unsafe;

    public TieredEnchantmentUpgrade(@NotNull String name, @NotNull EnchantmentTarget target, @NotNull Enchantment ench, int max, boolean unsafe) {
        super(name, max);

        Preconditions.checkNotNull(target, "TieredUpgrade's enchantment target cannot be null!");
        Preconditions.checkNotNull(ench, "TieredUpgrade's enchantment cannot be null!");

        if (!unsafe && max > ench.getMaxLevel())
            throw new IllegalArgumentException("TieredUpgrade's maximum tier exceeds the enchantment's limit!");

        this.ench = ench;
        this.target = target;
        this.unsafe = unsafe;
    }

    public TieredEnchantmentUpgrade(@NotNull String name, @NotNull EnchantmentTarget target, @NotNull Enchantment ench) {
        this(name, target, ench, ench.getMaxLevel(), false);
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
        if (current_tier == 0 || current_tier >= max)
            return false;

        int current_level = ench.getStartLevel() + current_tier - 1;
        for (GamePlayer team_player : team_players)
            EnchantmentUtils.enchant(team_player.getPlayer(), target, ench, current_level, unsafe);

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof TieredEnchantmentUpgrade))
            return false;

        if (!super.equals(obj))
            return false;

        TieredEnchantmentUpgrade other = (TieredEnchantmentUpgrade) obj;
        return unsafe == other.unsafe && target == other.target && ench.equals(other.ench);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ench, target, unsafe);
    }

}