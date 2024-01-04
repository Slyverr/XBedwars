package com.slyvr.bedwars.generator;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.generator.ResourceGenerator;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractResourceGenerator implements ResourceGenerator {

    protected final Game game;

    protected Location loc;

    public AbstractResourceGenerator(@NotNull Game game, @NotNull Location loc) {
        Preconditions.checkNotNull(game, "ResourceGenerator's game cannot be null!");
        Preconditions.checkNotNull(loc, "ResourceGenerator's drop location cannot be null!");

        this.game = game;
        this.loc = loc.clone();
    }

    @Override
    public @NotNull Game getGame() {
        return game;
    }

    @Override
    public @NotNull Location getDropLocation() {
        return loc.clone();
    }

    @Override
    public void setDropLocation(@NotNull Location loc) {
        if (loc != null)
            this.loc = loc.clone();
    }

}
