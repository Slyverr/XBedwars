package com.slyvr.xbedwars.manager;

import com.slyvr.xbedwars.api.manager.UpgradesManager;
import com.slyvr.xbedwars.api.upgrade.TieredUpgrade;
import com.slyvr.xbedwars.api.upgrade.Upgrade;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class BedwarsUpgradesManager implements UpgradesManager {

    private static final Map<String, Upgrade> UPGRADES = new HashMap<>();
    private static final Map<String, TieredUpgrade> TIERED_UPGRADES = new HashMap<>();

    private static final BedwarsUpgradesManager INSTANCE = new BedwarsUpgradesManager();

    private BedwarsUpgradesManager() {
    }

    @Override
    public @NotNull Collection<Upgrade> getUpgrades() {
        return new HashSet<>(UPGRADES.values());
    }

    @Override
    public @NotNull Collection<TieredUpgrade> getTieredUpgrades() {
        return new HashSet<>(TIERED_UPGRADES.values());
    }

    @Override
    public @Nullable Upgrade getUpgrade(@NotNull String name) {
        return name != null ? UPGRADES.get(name.toLowerCase()) : null;
    }

    @Override
    public @Nullable TieredUpgrade getTieredUpgrade(@NotNull String name) {
        return name != null ? TIERED_UPGRADES.get(name.toLowerCase()) : null;
    }

    @Override
    public void registerUpgrade(@NotNull String name, @NotNull Upgrade upgrade) {
        if (name == null || upgrade == null)
            return;

        if (upgrade instanceof TieredUpgrade)
            TIERED_UPGRADES.putIfAbsent(name.toLowerCase(), (TieredUpgrade) upgrade);
        else
            UPGRADES.putIfAbsent(name.toLowerCase(), (upgrade));
    }

    public static UpgradesManager getInstance() {
        return INSTANCE;
    }

}