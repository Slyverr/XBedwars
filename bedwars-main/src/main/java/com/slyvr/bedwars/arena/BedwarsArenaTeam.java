package com.slyvr.bedwars.arena;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.arena.team.ArenaTeam;
import com.slyvr.bedwars.api.arena.team.ArenaTeamBed;
import com.slyvr.bedwars.api.team.TeamColor;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.slyvr.bedwars.api.shop.Shop.ShopType;


public final class BedwarsArenaTeam implements ArenaTeam {

    private final TeamColor owner;

    private Location spawn;
    private Location team_shop;
    private Location team_upgrade;
    private Location team_generator;
    private Chest team_chest;
    private ArenaTeamBed team_bed;


    public BedwarsArenaTeam(@NotNull TeamColor color) {
        Preconditions.checkNotNull(color, "Team's color cannot be null!");

        this.owner = color;
    }

    @Override
    public @NotNull TeamColor getColor() {
        return owner;
    }

    @Override
    public @Nullable ArenaTeamBed getBed() {
        return team_bed;
    }

    @Override
    public void setBed(@NotNull ArenaTeamBed bed) {
        if (bed != null && bed.getTeamColor() == owner)
            this.team_bed = bed;
    }

    @Override
    public @Nullable Location getSpawnLocation() {
        return spawn != null ? spawn.clone() : null;
    }

    @Override
    public void setSpawnLocation(@NotNull Location spawn) {
        if (spawn != null)
            this.spawn = spawn.clone();
    }

    @Override
    public @Nullable Location getShopNPCLocation(@NotNull ShopType type) {
        if (type == null)
            return null;

        switch (type) {
            case ITEMS:
                return team_shop != null ? team_shop.clone() : null;
            case UPGRADES:
                return team_upgrade != null ? team_upgrade.clone() : null;
        }

        return null;
    }

    @Override
    public void setShopNPCLocation(@NotNull ShopType type, @NotNull Location loc) {
        if (type == null || loc == null)
            return;

        if (type == ShopType.ITEMS)
            this.team_shop = loc.clone();
        else
            this.team_upgrade = loc.clone();
    }

    @Override
    public @Nullable Location getResourceGeneratorLocation() {
        return team_generator != null ? team_generator.clone() : null;
    }

    @Override
    public void setResourceGeneratorLocation(@NotNull Location loc) {
        if (loc != null)
            this.team_generator = loc.clone();
    }

    @Override
    public @Nullable Chest getChest() {
        return team_chest;
    }

    @Override
    public void setChest(@NotNull Chest chest) {
        if (chest != null)
            this.team_chest = chest;
    }

    @Override
    public boolean isReady() {
        if (spawn == null)
            return false;

        if (team_bed == null)
            return false;

        if (team_generator == null)
            return false;

        return team_shop != null && team_upgrade != null;
    }

}