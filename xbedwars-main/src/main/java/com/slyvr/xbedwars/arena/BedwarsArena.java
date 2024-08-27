package com.slyvr.xbedwars.arena;

import com.google.common.base.Preconditions;
import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.api.arena.Arena;
import com.slyvr.xbedwars.api.arena.generator.ArenaResourceGeneratorManager;
import com.slyvr.xbedwars.api.arena.team.ArenaTeam;
import com.slyvr.xbedwars.api.arena.team.ArenaTeamBed;
import com.slyvr.xbedwars.api.game.GameMode;
import com.slyvr.xbedwars.api.game.phase.GamePhasePreset;
import com.slyvr.xbedwars.api.generator.Resource;
import com.slyvr.xbedwars.api.generator.team.TeamResourceGeneratorPreset;
import com.slyvr.xbedwars.api.generator.tiered.TieredResourceGeneratorPreset;
import com.slyvr.xbedwars.api.manager.ArenasManager;
import com.slyvr.xbedwars.api.shop.Shop.ShopType;
import com.slyvr.xbedwars.api.team.TeamColor;
import com.slyvr.xbedwars.configuration.Configuration;
import com.slyvr.xbedwars.manager.BedwarsArenasManager;
import com.slyvr.xbedwars.utils.BedUtils;
import com.slyvr.xbedwars.utils.ConfigurationUtils;
import com.slyvr.xbedwars.utils.LocationUtils;
import com.slyvr.xbedwars.utils.Region;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public final class BedwarsArena extends Configuration implements Arena {

    private final Map<TeamColor, ArenaTeam> arena_teams = new EnumMap<>(TeamColor.class);

    private final ArenaResourceGeneratorManager manager;
    private final String name;

    private GamePhasePreset phases_preset;
    private Location bosses_spawn_location;
    private Location spectators_location;
    private Location waiting_location;
    private Region waiting_region;
    private Region arena_region;
    private String map_name;
    private GameMode mode;

    private int time;
    private boolean enabled;


    public BedwarsArena(@NotNull String name) {
        super(new File(XBedwars.getInstance().getDataFolder() + "/Arenas", name + ".yml"));

        Preconditions.checkNotNull(name, "Arena's name cannot be null!");

        this.name = name;
        this.manager = new BedwarsArenaResourceGeneratorManager();
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @Nullable String getMapName() {
        return map_name;
    }

    @Override
    public void setMapName(@NotNull String name) {
        if (name != null)
            this.map_name = name;
    }

    @Override
    public int getTime() {
        return time;
    }

    @Override
    public void setTime(int time) {
        if (time >= 0)
            this.time = time;
    }

    @Override
    public @Nullable GameMode getMode() {
        return mode;
    }

    @Override
    public void setMode(@NotNull GameMode mode) {
        if (mode != null)
            this.mode = mode;
    }

    @Override
    public @NotNull GamePhasePreset getPhasesPreset() {
        return phases_preset;
    }

    @Override
    public void setPhasesPreset(@NotNull GamePhasePreset preset) {
        if (preset != null)
            this.phases_preset = preset;
    }

    @Override
    public @NotNull Set<ArenaTeam> getReadyTeams() {
        Set<ArenaTeam> result = new HashSet<>(arena_teams.size());

        for (ArenaTeam team : arena_teams.values()) {
            if (team.isReady())
                result.add(team);
        }

        return result;
    }

    @Override
    public @NotNull ArenaTeam getTeam(@NotNull TeamColor color) {
        Preconditions.checkNotNull(color, "Cannot get the team of a null color!");

        return arena_teams.computeIfAbsent(color, BedwarsArenaTeam::new);
    }

    @Override
    public @NotNull ArenaResourceGeneratorManager getResourceGeneratorManager() {
        return manager;
    }

    @Override
    public @Nullable Location getBossSpawnLocation() {
        return bosses_spawn_location != null ? bosses_spawn_location.clone() : null;
    }

    @Override
    public void setBossSpawnLocation(@NotNull Location loc) {
        if (loc != null)
            this.bosses_spawn_location = loc.clone();
    }

    @Override
    public @Nullable Location getSpectatorSpawnLocation() {
        return spectators_location != null ? spectators_location.clone() : null;
    }

    @Override
    public void setSpectatorSpawnLocation(@NotNull Location loc) {
        if (loc != null)
            this.spectators_location = loc.clone();
    }

    @Override
    public @Nullable Location getWaitingRoomSpawnLocation() {
        return this.waiting_location != null ? this.waiting_location.clone() : null;
    }

    @Override
    public void setWaitingRoomSpawnLocation(@NotNull Location loc) {
        if (loc != null)
            this.waiting_location = loc.clone();
    }

    @Override
    public @Nullable Region getWaitingRoomRegion() {
        return waiting_region;
    }

    @Override
    public void setWaitingRoomRegion(@NotNull Region region) {
        if (region != null)
            this.waiting_region = region;
    }

    @Override
    public @Nullable Region getRegion() {
        return arena_region;
    }

    @Override
    public void setRegion(@NotNull Region region) {
        if (region != null)
            this.arena_region = region;
    }

    @Override
    public int getReadyTeamsCount() {
        int result = 0;

        for (ArenaTeam team : arena_teams.values()) {
            if (team.isReady())
                result++;
        }

        return result;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isReady() {
        if (!enabled)
            return false;

        if (mode == null)
            return false;

        if (spectators_location == null)
            return false;

        if (waiting_location == null)
            return false;

        if (arena_region == null)
            return false;

        if (manager.getTeamResourceGeneratorPreset() == null)
            return false;

        return isEnoughReadyTeams();
    }

    private boolean isEnoughReadyTeams() {
        if (arena_teams.size() < 2)
            return false;

        int readyCount = 0;
        for (ArenaTeam team : arena_teams.values()) {
            if (!team.isReady())
                continue;

            readyCount++;
            if (readyCount >= 2)
                return true;
        }

        return false;
    }

    @Override
    public void loadData() {
        this.reload();

        // Loading arena's preferences
        this.map_name = config.getString("Arena-Preferences.Map-Name");
        this.time = config.getInt("Arena-Preferences.Time", 1000);

        // Loading arena's settings
        this.mode = GameMode.getByString(config.getString("Arena-Settings.Mode"));
        this.enabled = config.getBoolean("Arena-Settings.Enabled");

        this.manager.setTeamResourceGeneratorSplittingRadius(config.getDouble("Arena-Settings.Resource-Split.Radius", 1.5));
        this.manager.setTeamResourceGeneratorSplitting(config.getBoolean("Arena-Settings.Resource-Split.Enabled", true));

        // Loading arena's regions
        this.waiting_region = ConfigurationUtils.getRegion(config, "Arena-Regions.Waiting-Room");
        this.arena_region = ConfigurationUtils.getRegion(config, "Arena-Regions.Map");

        // Loading arena's spawns
        this.waiting_location = ConfigurationUtils.getLocation(config, "Arena-Spawns.Waiting-Room");
        this.spectators_location = ConfigurationUtils.getLocation(config, "Arena-Spawns.Spectators");
        this.bosses_spawn_location = ConfigurationUtils.getLocation(config, "Arena-Spawns.Bosses");

        this.loadArenaTeams();
        this.loadArenaPresets();
        this.loadArenaResourceGenerators();
    }

    @Override
    public void saveData() {
        this.config = new YamlConfiguration();

        // Storing arena's preferences
        ConfigurationSection preferences = config.createSection("Arena-Preferences");

        preferences.set("Map-Name", map_name);
        preferences.set("Time", time);

        // Storing arena's settings
        ConfigurationSection settings = config.createSection("Arena-Settings");

        settings.set("Enabled", enabled);
        settings.set("Mode", mode != null ? mode.getName() : null);

        // Storing arena's presets
        ConfigurationSection presets = config.createSection("Arena-Presets");

        TeamResourceGeneratorPreset teams_preset = manager.getTeamResourceGeneratorPreset();
        if (teams_preset != null)
            presets.set("Teams-Resource-Generator-Preset", teams_preset.getName());

        for (Resource resource : Resource.values()) {
            TieredResourceGeneratorPreset resource_preset = manager.getTieredResourceGeneratorPreset(resource);
            if (resource_preset != null)
                presets.set("Tiered-Resource-Generator-Presets." + resource.getName(), resource_preset.getName());
        }

        if (phases_preset != null)
            presets.set("Game-Phases-Preset", phases_preset.getName());

        // Storing arena's regions
        ConfigurationSection regions = config.createSection("Arena-Regions");

        if (waiting_region != null) {
            regions.set("Waiting-Room.pos-1", LocationUtils.serialize(waiting_region.getFirstPosition(), false));
            regions.set("Waiting-Room.pos-2", LocationUtils.serialize(waiting_region.getSecondPosition(), false));
        }

        if (arena_region != null) {
            regions.set("Map.pos-1", LocationUtils.serialize(arena_region.getFirstPosition(), false));
            regions.set("Map.pos-2", LocationUtils.serialize(arena_region.getSecondPosition(), false));
        }

        // Storing arena's spawns
        ConfigurationSection spawns = config.createSection("Arena-Spawns");

        if (waiting_location != null)
            spawns.set("Waiting-Room", LocationUtils.serialize(waiting_location, true));

        if (spectators_location != null)
            spawns.set("Spectators", LocationUtils.serialize(spectators_location, true));

        if (bosses_spawn_location != null)
            spawns.set("Bosses", LocationUtils.serialize(bosses_spawn_location, true));

        // Storing arena's teams
        for (ArenaTeam arena_team : arena_teams.values()) {
            TeamColor color = arena_team.getColor();

            // Storing team's spawn-location
            Location spawn = arena_team.getSpawnLocation();
            if (spawn != null)
                this.config.set("Arena-Teams." + color + ".Team-Spawn", LocationUtils.serialize(spawn, true));

            // Storing team's shops locations
            Location items_shop = arena_team.getShopNPCLocation(ShopType.ITEMS);
            if (items_shop != null)
                this.config.set("Arena-Teams." + color + ".Team-Shop.items", LocationUtils.serialize(items_shop, true));

            Location upgrades_shop = arena_team.getShopNPCLocation(ShopType.UPGRADES);
            if (upgrades_shop != null)
                this.config.set("Arena-Teams." + color + ".Team-Shop.upgrades", LocationUtils.serialize(upgrades_shop, true));

            // Storing team's bed
            ArenaTeamBed bed = arena_team.getBed();
            if (bed != null) {
                this.config.set("Arena-Teams." + color + ".Team-Bed.head", LocationUtils.serialize(bed.getHead().getLocation(), false));
                this.config.set("Arena-Teams." + color + ".Team-Bed.foot", LocationUtils.serialize(bed.getFoot().getLocation(), false));
            }

            // Storing team's chest
            Chest chest = arena_team.getChest();
            if (chest != null)
                this.config.set("Arena-Teams." + color + ".Team-Chest", LocationUtils.serialize(chest.getLocation(), false));

            // Storing team's resource-generator
            Location resource_generator = arena_team.getResourceGeneratorLocation();
            if (resource_generator != null)
                this.config.set("Arena-Teams." + color + ".Team-Resource-Generator", LocationUtils.serialize(resource_generator, false));
        }

        // Storing arena's resource generators
        for (Resource resource : Resource.values()) {
            List<Location> locations_list = manager.getResourceGeneratorLocations(resource);
            if (locations_list.isEmpty())
                continue;

            List<String> locations = new ArrayList<>(locations_list.size());
            for (Location loc : locations_list)
                locations.add(LocationUtils.serialize(loc, false));

            this.config.set("Arena-Resource-Generators." + resource.getName(), locations);
        }

        this.save();
    }

    private void loadArenaTeams() {
        ConfigurationSection teams_section = config.getConfigurationSection("Arena-Teams");
        if (teams_section == null)
            return;

        for (String teams_section_key : teams_section.getKeys(false)) {
            TeamColor color = TeamColor.getByName(teams_section_key);
            if (color == null)
                continue;

            BedwarsArenaTeam arena_team = new BedwarsArenaTeam(color);

            arena_team.setSpawnLocation(ConfigurationUtils.getLocation(teams_section, color + ".Team-Spawn"));

            arena_team.setShopNPCLocation(ShopType.ITEMS, ConfigurationUtils.getLocation(teams_section, color + ".Team-Shop.items"));
            arena_team.setShopNPCLocation(ShopType.UPGRADES, ConfigurationUtils.getLocation(teams_section, color + ".Team-Shop.upgrades"));

            arena_team.setResourceGeneratorLocation(ConfigurationUtils.getLocation(teams_section, color + ".Team-Resource-Generator"));

            Location team_chest = ConfigurationUtils.getLocation(teams_section, color + ".Team-Chest");
            if (team_chest != null) {
                Block block = team_chest.getBlock();
                if (block.getType() == Material.CHEST)
                    arena_team.setChest((Chest) block.getState());
            }

            Location bed_head = ConfigurationUtils.getLocation(teams_section, color + ".Team-Bed.head");
            Location bed_foot = ConfigurationUtils.getLocation(teams_section, color + ".Team-Bed.foot");

            if (bed_head != null && bed_foot != null && BedUtils.isBed(bed_head.getBlock(), bed_foot.getBlock()))
                arena_team.setBed(BedwarsArenaTeamBed.create(bed_head.getBlock(), color));

            this.arena_teams.put(color, arena_team);
        }

    }

    private void loadArenaPresets() {
        ConfigurationSection presets_section = config.getConfigurationSection("Arena-Presets");
        if (presets_section == null)
            presets_section = config.createSection("Arena-Presets");

        this.phases_preset = GamePhasePreset.getByName(presets_section.getString("Game-Phases-Preset", "Default"));

        TeamResourceGeneratorPreset teams_preset = TeamResourceGeneratorPreset.getByName(presets_section.getString("Teams-Resource-Generator-Preset"));
        this.manager.setTeamResourceGeneratorPreset(teams_preset);

        ConfigurationSection tiered_presets = presets_section.getConfigurationSection("Tiered-Resource-Generator-Presets");
        if (tiered_presets == null)
            return;

        for (String tiered_presets_section_key : tiered_presets.getKeys(false)) {
            Resource resource = Resource.getByName(tiered_presets_section_key);
            if (resource == null)
                continue;

            TieredResourceGeneratorPreset preset = TieredResourceGeneratorPreset.getByName(tiered_presets.getString(tiered_presets_section_key));
            if (preset != null)
                this.manager.setTieredResourceGeneratorPreset(resource, preset);
        }

    }

    private void loadArenaResourceGenerators() {
        ConfigurationSection generators_section = config.getConfigurationSection("Arena-Resource-Generators");
        if (generators_section == null)
            return;

        for (String generators_section_key : generators_section.getKeys(false)) {
            Resource resource = Resource.getByName(generators_section_key);
            if (resource == null || resource == Resource.FREE)
                continue;

            for (Location location : ConfigurationUtils.getLocationList(generators_section, generators_section_key))
                this.manager.addResourceGenerator(resource, location);
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        BedwarsArena other = (BedwarsArena) obj;
        return name.equalsIgnoreCase(other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }

    @Override
    public String toString() {
        return "xbedwarsArena{Name='" + name + '}';
    }

    public static void loadArenas() {
        String[] files_names = new File(XBedwars.getInstance().getDataFolder() + "/Arenas").list();
        if (files_names == null)
            return;

        ArenasManager manager = BedwarsArenasManager.getInstance();
        for (String file_name : files_names) {
            if (!file_name.toLowerCase().endsWith(".yml"))
                continue;

            Arena arena = manager.create(file_name.substring(0, file_name.length() - 4));
            arena.loadData();
        }

    }

}