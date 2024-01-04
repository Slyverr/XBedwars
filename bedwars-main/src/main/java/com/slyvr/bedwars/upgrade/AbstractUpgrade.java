package com.slyvr.bedwars.upgrade;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.upgrade.Upgrade;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractUpgrade implements Upgrade {

    private final String name;

    public AbstractUpgrade(String name) {
        Preconditions.checkNotNull(name, "Upgrade's name cannot be null!");

        this.name = name;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

}