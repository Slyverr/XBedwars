package com.slyvr.bedwars.team;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.generator.team.TeamResourceGenerator;
import com.slyvr.bedwars.api.team.GameTeam;
import com.slyvr.bedwars.api.team.GameTeamTrapManager;
import com.slyvr.bedwars.api.team.GameTeamUpgradeManager;
import com.slyvr.bedwars.api.team.TeamColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public final class BedwarsTeam implements GameTeam {

    private final GameTeamUpgradeManager upgrades;
    private final GameTeamTrapManager traps;

    private final Game game;
    private final TeamColor color;
    private final TeamResourceGenerator generator;


    public BedwarsTeam(@NotNull Game game, @NotNull TeamColor color, @NotNull TeamResourceGenerator generator) {
        Preconditions.checkNotNull(game, "Team's game cannot be null!");
        Preconditions.checkNotNull(color, "Team's color cannot be null!");
        Preconditions.checkNotNull(generator, "Team's resource-generator cannot be null!");

        this.game = game;
        this.color = color;
        this.generator = generator;

        this.upgrades = new BedwarsTeamUpgradeManager();
        this.traps = new BedwarsTeamTrapManager();
    }

    @Override
    public @NotNull Game getGame() {
        return game;
    }

    @Override
    public @NotNull TeamColor getColor() {
        return color;
    }

    @Override
    public @NotNull TeamResourceGenerator getResourceGenerator() {
        return generator;
    }

    @Override
    public @NotNull GameTeamUpgradeManager getUpgradeManager() {
        return upgrades;
    }

    @Override
    public @NotNull GameTeamTrapManager getTrapManager() {
        return traps;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof BedwarsTeam))
            return false;

        BedwarsTeam other = (BedwarsTeam) obj;
        return color == other.color && game.equals(other.game);
    }

    @Override
    public int hashCode() {
        return Objects.hash(game, color);
    }

    @Override
    public String toString() {
        return "BedwarsTeam{" +
                "game=" + game +
                ", color=" + color +
                '}';
    }

}