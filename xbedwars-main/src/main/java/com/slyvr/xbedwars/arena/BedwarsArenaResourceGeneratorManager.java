package com.slyvr.xbedwars.arena;

import com.slyvr.xbedwars.api.arena.generator.ArenaResourceGeneratorManager;
import com.slyvr.xbedwars.api.generator.Resource;
import com.slyvr.xbedwars.api.generator.team.TeamResourceGeneratorPreset;
import com.slyvr.xbedwars.api.generator.tiered.TieredResourceGeneratorPreset;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class BedwarsArenaResourceGeneratorManager implements ArenaResourceGeneratorManager {

    private final Map<Resource, ResourceGeneratorStorage> generators_storage = new HashMap<>();
    private TeamResourceGeneratorPreset teams_preset;

    private double resource_splitting_radius = 1.5;
    private boolean resource_splitting = true;

    public BedwarsArenaResourceGeneratorManager() {
    }

    @Override
    public @NotNull List<Location> getResourceGeneratorLocations(@NotNull Resource resource) {
        ResourceGeneratorStorage storage = generators_storage.get(resource);
        return storage != null ? storage.getLocations() : new ArrayList<>(0);
    }

    @Override
    public void addResourceGenerator(@NotNull Resource resource, @NotNull Location loc) {
        if (resource != null && loc != null)
            this.generators_storage.computeIfAbsent(resource, rsc -> new ResourceGeneratorStorage()).addLocation(loc);
    }

    @Override
    public boolean removeResourceGenerator(@NotNull Resource resource, int index) {
        ResourceGeneratorStorage storage = generators_storage.get(resource);
        return storage != null && storage.removeLocation(index);
    }

    @Override
    public @Nullable TieredResourceGeneratorPreset getTieredResourceGeneratorPreset(@NotNull Resource resource) {
        ResourceGeneratorStorage storage = generators_storage.get(resource);
        return storage != null ? storage.getPreset() : null;
    }

    @Override
    public void setTieredResourceGeneratorPreset(@NotNull Resource resource, @NotNull TieredResourceGeneratorPreset preset) {
        if (resource != null && preset != null)
            this.generators_storage.computeIfAbsent(resource, rsc -> new ResourceGeneratorStorage()).setPreset(preset);
    }

    @Override
    public @Nullable TeamResourceGeneratorPreset getTeamResourceGeneratorPreset() {
        return teams_preset;
    }

    @Override
    public void setTeamResourceGeneratorPreset(@NotNull TeamResourceGeneratorPreset preset) {
        if (preset != null)
            this.teams_preset = preset;
    }

    @Override
    public double getTeamResourceGeneratorSplittingRadius() {
        return resource_splitting_radius;
    }

    @Override
    public void setTeamResourceGeneratorSplittingRadius(double radius) {
        if (radius > 0)
            this.resource_splitting_radius = radius;
    }

    @Override
    public boolean isTeamResourceGeneratorSplitting() {
        return resource_splitting;
    }

    @Override
    public void setTeamResourceGeneratorSplitting(boolean split) {
        this.resource_splitting = split;
    }

    private static final class ResourceGeneratorStorage {

        private final List<Location> locations = new ArrayList<>();
        private TieredResourceGeneratorPreset preset;

        public ResourceGeneratorStorage() {
        }

        @NotNull
        public List<Location> getLocations() {
            return new ArrayList<>(locations);
        }


        public void addLocation(@NotNull Location loc) {
            this.locations.add(loc);
        }


        public boolean removeLocation(int index) {
            return index >= 0 && index < locations.size() && locations.remove(index) != null;
        }


        @Nullable
        public TieredResourceGeneratorPreset getPreset() {
            return preset;
        }


        public void setPreset(@NotNull TieredResourceGeneratorPreset preset) {
            this.preset = preset;
        }

    }

}