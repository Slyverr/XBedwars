package com.slyvr.xbedwars.trap;

import com.google.common.base.Preconditions;
import com.slyvr.xbedwars.api.trap.Trap;
import com.slyvr.xbedwars.api.trap.TrapTarget;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class AbstractTrap implements Trap {

    protected String name;
    protected TrapTarget target;

    protected int duration;

    public AbstractTrap(String name, TrapTarget target, int duration) {
        Preconditions.checkNotNull(name, "Trap name cannot be null!");
        Preconditions.checkNotNull(target, "Trap target cannot be null!");

        this.name = name;
        this.target = target;
        this.duration = duration;
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public @NotNull TrapTarget getTarget() {
        return this.target;
    }

    @Override
    public int getDuration() {
        return this.duration;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name.toLowerCase(), this.target);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof AbstractTrap))
            return false;

        AbstractTrap other = (AbstractTrap) obj;
        return this.target == other.target && this.name.equalsIgnoreCase(other.name);
    }

}
