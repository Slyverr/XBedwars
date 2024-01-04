package com.slyvr.bedwars.manager;

import com.slyvr.bedwars.api.manager.TrapsManager;
import com.slyvr.bedwars.api.trap.Trap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class BedwarsTrapsManager implements TrapsManager {

    private static final BedwarsTrapsManager INSTANCE = new BedwarsTrapsManager();
    private static final Map<String, Trap> TRAPS = new HashMap<>();

    private BedwarsTrapsManager() {
    }

    public static BedwarsTrapsManager getInstance() {
        return INSTANCE;
    }

    @Override
    public @NotNull Collection<Trap> getTraps() {
        return new HashSet<>(TRAPS.values());
    }

    @Override
    public @Nullable Trap getTrap(@NotNull String name) {
        return name != null ? TRAPS.get(name.toLowerCase()) : null;
    }

    @Override
    public void registerTrap(@NotNull String name, @NotNull Trap trap) {
        if (name == null || trap == null)
            return;

        TRAPS.putIfAbsent(name.toLowerCase(), trap);
    }

}
