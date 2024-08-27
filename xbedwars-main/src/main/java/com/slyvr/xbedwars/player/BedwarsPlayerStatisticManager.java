package com.slyvr.xbedwars.player;

import com.slyvr.xbedwars.api.player.GamePlayerStatisticManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class BedwarsPlayerStatisticManager implements GamePlayerStatisticManager {

    private final Map<GamePlayerStatistic, Integer> stats = new HashMap<>();

    public BedwarsPlayerStatisticManager() {
    }

    @Override
    public @NotNull Map<GamePlayerStatistic, Integer> getStats() {
        return this.stats;
    }

    @Override
    public int getStatistic(@NotNull GamePlayerStatistic stat) {
        if (stat == null)
            return 0;

        Integer old = this.stats.get(stat);
        return old != null ? old : 0;
    }

    @Override
    public void incrementStatistic(@NotNull GamePlayerStatistic stat, int value) {
        if (stat == null || value <= 0)
            return;

        this.stats.merge(stat, value, Integer::sum);
    }

    @Override
    public void decrementStatistic(@NotNull GamePlayerStatistic stat, int value) {
        if (stat == null || value <= 0)
            return;

        Integer old = this.stats.get(stat);
        if (old == null)
            return;

        this.stats.put(stat, Math.max(old - value, 0));
    }

    @Override
    public void setStatistic(@NotNull GamePlayerStatistic stat, int value) {
        if (stat != null && value >= 0)
            this.stats.put(stat, value);
    }

}