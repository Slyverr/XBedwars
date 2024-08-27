package com.slyvr.xbedwars.configuration;

import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.api.generator.Resource;
import com.slyvr.xbedwars.api.generator.team.TeamResourceGeneratorDrop;
import com.slyvr.xbedwars.api.generator.team.TeamResourceGeneratorPreset;
import com.slyvr.xbedwars.api.generator.tiered.ResourceGeneratorTier;
import com.slyvr.xbedwars.api.generator.tiered.TieredResourceGeneratorPreset;
import com.slyvr.xbedwars.api.lang.NamesRegistry;
import com.slyvr.xbedwars.utils.ConfigurationUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public final class PresetsConfig extends Configuration {

    private static final PresetsConfig INSTANCE = new PresetsConfig();

    private boolean isLoaded;


    private PresetsConfig() {
        super("Presets.yml");

        this.setDefaultResource();
    }

    @NotNull
    public static PresetsConfig getInstance() {
        return INSTANCE;
    }

    public void loadPresets() {
        if (isLoaded)
            return;

        this.loadTieredGeneratorPresets();
        this.loadTeamGeneratorPresets();

        this.isLoaded = true;
    }

    @Nullable
    private TeamResourceGeneratorDrop getGeneratorDrop(@NotNull ConfigurationSection section) {
        if (section == null)
            return null;

        Resource resource = Resource.getByName(section.getName());
        if (resource == null)
            return null;

        int dps = section.getInt("drops-per-minute");
        if (dps <= 0)
            return null;

        int drop_limit = section.getInt("drops-limit");
        if (drop_limit <= 0)
            return null;

        return new TeamResourceGeneratorDrop(resource, dps, drop_limit, section.getBoolean("drops-split"));
    }

    @Nullable
    private ResourceGeneratorTier getGeneratorTier(@NotNull ConfigurationSection section) {
        if (section == null)
            return null;

        NamesRegistry names = ConfigurationUtils.getNamesRegistry(section, "title", true);
        if (names.getDefaultName().equals("title"))
            return null;

        int drop_time = section.getInt("drops-time");
        if (drop_time <= 0)
            return null;

        int drop_limit = section.getInt("drops-limit");
        if (drop_limit <= 0)
            return null;

        return new ResourceGeneratorTier(names, drop_time, drop_limit);
    }

    private void loadTieredGeneratorPresets() {
        ConfigurationSection presets_section = config.getConfigurationSection("Tiered-Resource-Generator-Presets");
        if (presets_section == null)
            presets_section = config.getDefaults().getConfigurationSection("Team-Resource-Generator-Presets");

        for (String preset_name : presets_section.getKeys(false)) {
            ConfigurationSection tiers_section = presets_section.getConfigurationSection(preset_name);
            if (tiers_section == null)
                continue;

            List<ResourceGeneratorTier> tiers = new ArrayList<>();
            for (String tiers_section_key : tiers_section.getKeys(false)) {
                ResourceGeneratorTier tier = getGeneratorTier(tiers_section.getConfigurationSection(tiers_section_key));
                if (tier != null)
                    tiers.add(tier);
            }

            if (!tiers.isEmpty())
                TieredResourceGeneratorPreset.register(new TieredResourceGeneratorPreset(preset_name, tiers));
        }

    }

    private void loadTeamGeneratorPresets() {
        ConfigurationSection presets_section = config.getConfigurationSection("Team-Resource-Generator-Presets");
        if (presets_section == null)
            presets_section = config.getDefaults().getConfigurationSection("Team-Resource-Generator-Presets");

        for (String preset_name : presets_section.getKeys(false)) {
            ConfigurationSection resources_section = presets_section.getConfigurationSection(preset_name);
            if (resources_section == null)
                continue;

            Set<TeamResourceGeneratorDrop> drops = new HashSet<>();
            for (String resource_name : resources_section.getKeys(false)) {
                TeamResourceGeneratorDrop drop = getGeneratorDrop(resources_section.getConfigurationSection(resource_name));
                if (drop != null)
                    drops.add(drop);
            }

            if (!drops.isEmpty())
                TeamResourceGeneratorPreset.register(new TeamResourceGeneratorPreset(preset_name, drops));
        }

    }

    private void setDefaultResource() {
        InputStream stream = XBedwars.getInstance().getResource("Presets.yml");
        if (stream == null)
            return;

        this.config.options().copyDefaults(true);
        this.config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(stream)));
    }

}