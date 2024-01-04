package com.slyvr.bedwars.team;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.team.GameTeamTrapManager;
import com.slyvr.bedwars.api.trap.Trap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class BedwarsTeamTrapManager implements GameTeamTrapManager {

    private final List<Trap> traps;
    private final int limit;

    public BedwarsTeamTrapManager(int limit) {
        Preconditions.checkArgument(limit >= 1, "Traps limit must be at least 1!");

        this.limit = limit;
        this.traps = new ArrayList<>(limit);
    }

    public BedwarsTeamTrapManager() {
        this.traps = new ArrayList<>(limit = 3);
    }

    @Override
    public @NotNull List<Trap> getTraps() {
        return new ArrayList<>(traps);
    }

    @Override
    public boolean addTrap(@NotNull Trap trap) {
        return trap != null && traps.size() < limit && traps.add(trap);
    }

    @Override
    public boolean removeTrap(@NotNull Trap trap) {
        return trap != null && traps.remove(trap);
    }

    @Override
    public boolean contains(@NotNull Trap trap) {
        return trap != null && traps.contains(trap);
    }

    @Override
    public int size() {
        return traps.size();
    }

}