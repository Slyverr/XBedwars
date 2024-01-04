package com.slyvr.bedwars.upgrade.custom;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.api.team.GameTeam;
import com.slyvr.bedwars.api.upgrade.Upgrade;
import com.slyvr.bedwars.upgrade.AbstractUpgrade;
import com.slyvr.bedwars.utils.EnchantmentUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class EnchantmentUpgrade extends AbstractUpgrade implements Upgrade {

    public static final Upgrade SHARPNESS_UPGRADE = new EnchantmentUpgrade("Sharpness", EnchantmentTarget.WEAPON, Enchantment.DAMAGE_ALL, 1);

    private final EnchantmentTarget target;
    private final Enchantment ench;
    private final boolean unsafe;
    private final int level;

    public EnchantmentUpgrade(@NotNull String name, @NotNull EnchantmentTarget target, @NotNull Enchantment ench, int level, boolean unsafe) {
        super(name);

        Preconditions.checkNotNull(target, "Upgrade's enchantment target cannot be null!");
        Preconditions.checkNotNull(ench, "Upgrade's enchantment cannot be null!");

        if (!unsafe && level > ench.getMaxLevel())
            throw new IllegalArgumentException("Upgrade's enchantment level exceeds the enchantment's limit!");

        this.ench = ench;
        this.target = target;
        this.unsafe = unsafe;
        this.level = ench.getStartLevel() + level - 1;
    }

    public EnchantmentUpgrade(@NotNull String name, @NotNull EnchantmentTarget target, @NotNull Enchantment ench, int level) {
        this(name, target, ench, level, false);
    }

    public EnchantmentUpgrade(@NotNull String name, @NotNull Enchantment ench, int level) {
        this(name, EnchantmentTarget.ALL, ench, level, false);
    }

    @Override
    public boolean apply(@NotNull GameTeam team) {
        if (team == null)
            return false;

        Game game = team.getGame();
        Collection<GamePlayer> team_players = game.getTeamPlayers(team.getColor());
        if (team_players.isEmpty())
            return false;

        for (GamePlayer team_player : team_players)
            EnchantmentUtils.enchant(team_player.getPlayer(), target, ench, level, unsafe);

        return true;
    }

}
