package com.slyvr.bedwars.team;

import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.api.team.GameTeamUpgradeManager;
import com.slyvr.bedwars.api.upgrade.TieredUpgrade;
import com.slyvr.bedwars.api.upgrade.Upgrade;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class BedwarsTeamUpgradeManager implements GameTeamUpgradeManager {

    private final Map<Upgrade, Integer> upgrades = new HashMap<>(4);

    public BedwarsTeamUpgradeManager() {
    }

    @Override
    public @NotNull Set<Upgrade> getUpgrades() {
        return new HashSet<>(upgrades.keySet());
    }

    @Override
    public boolean addUpgrade(@NotNull Upgrade upgrade) {
        if (upgrade == null)
            return false;

        this.upgrades.put(upgrade, 0);
        return true;
    }

    @Override
    public boolean removeUpgrade(@NotNull Upgrade upgrade) {
        return upgrade != null && upgrades.remove(upgrade) != null;
    }

    @Override
    public int getCurrentTier(@NotNull TieredUpgrade upgrade) {
        return upgrade != null ? upgrades.getOrDefault(upgrade, 0) : 0;
    }

    @Override
    public void setCurrentTier(@NotNull TieredUpgrade upgrade, int tier) {
        if (upgrade != null && tier >= 0 && tier <= upgrade.getMaximumTier())
            this.upgrades.put(upgrade, tier);
    }

    @Override
    public void apply(@NotNull GamePlayer player) {
        this.apply(player, null);
    }

    @Override
    public void apply(@NotNull GamePlayer player, Predicate<Upgrade> predicate) {
        if (player == null)
            return;

        for (Upgrade upgrade : upgrades.keySet()) {
            if (predicate == null || predicate.test(upgrade))
                upgrade.apply(player.getTeam());
        }

    }

    @Override
    public boolean contains(@NotNull Upgrade upgrade) {
        return upgrade != null && upgrades.containsKey(upgrade);
    }

}