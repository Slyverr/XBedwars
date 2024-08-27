package com.slyvr.xbedwars.upgrade;

import com.google.common.base.Preconditions;
import com.slyvr.xbedwars.api.upgrade.TieredUpgrade;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class AbstractTieredUpgrade implements TieredUpgrade {

    protected final String name;
    protected final int max;

    protected AbstractTieredUpgrade(@NotNull String name, int max) {
        Preconditions.checkNotNull(name, "TieredUpgrade's name cannot be null!");
        Preconditions.checkArgument(max > 1, "TieredUpgrade's number of tiers must be at least 2!");

        this.name = name;
        this.max = max;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public int getMaximumTier() {
        return max;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof AbstractTieredUpgrade))
            return false;

        AbstractTieredUpgrade other = (AbstractTieredUpgrade) obj;
        return max == other.max && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase(), max);
    }

}