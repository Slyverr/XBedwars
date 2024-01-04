package com.slyvr.bedwars.utils;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Preconditions;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.bedwars.api.game.GameMode;
import com.slyvr.bedwars.api.lang.Language;
import com.slyvr.bedwars.api.lang.NamesRegistry;
import com.slyvr.bedwars.api.shop.content.data.PurchasableCost;
import com.slyvr.bedwars.api.shop.content.data.PurchasableCosts;
import com.slyvr.bedwars.api.shop.content.data.PurchasableDescription;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class ConfigurationUtils {

    private ConfigurationUtils() {
    }


    @NotNull
    public static NamesRegistry getNamesRegistry(@NotNull ConfigurationSection section, @NotNull String path, boolean color) {
        ConfigurationSection names_section = section.getConfigurationSection(path);
        if (names_section == null)
            return new NamesRegistry(section.getString(path, section.getName()));

        Map<Language, String> names = new HashMap<>();
        for (String language_code : names_section.getKeys(false)) {
            Language lang = Language.getByString(language_code);
            if (lang == null)
                continue;

            String name = names_section.getString(language_code);
            if (name != null && !name.isEmpty())
                names.put(lang, color ? ChatUtils.format(name) : name);
        }

        return new NamesRegistry(names, section.getName());
    }

    // Shop Displayable

    @NotNull
    public static NamesRegistry getPurchasableNames(@NotNull ConfigurationSection section) {
        return getNamesRegistry(section, "purchasable-name", false);
    }

    @NotNull
    public static NamesRegistry getCategoryNames(@NotNull ConfigurationSection section) {
        return getNamesRegistry(section, "Display-Name", false);
    }

    @Nullable
    public static ItemStack getCategoryIcon(@NotNull ConfigurationSection section) {
        try {
            return XMaterial.matchXMaterial(section.getString("Display-Icon")).orElse(null).parseItem();
        } catch (Exception ignored) {
            return XMaterial.WRITABLE_BOOK.parseItem();
        }
    }


    // Shop Purchasables
    @NotNull
    public static PurchasableDescription getPurchasableDescription(@NotNull ConfigurationSection section) {
        ConfigurationSection description_section = section.getConfigurationSection("purchasable-description");
        if (description_section == null)
            return PurchasableDescription.EMPTY;

        Map<Language, String> descriptions = new HashMap<>();
        for (String description_section_key : description_section.getKeys(false)) {
            Language lang = Language.getByString(description_section_key);
            if (lang == null)
                continue;

            String description = description_section.getString(description_section_key);
            if (description == null || description.trim().isEmpty())
                continue;

            descriptions.put(lang, description);
        }

        return !descriptions.isEmpty() ? new PurchasableDescription(descriptions) : PurchasableDescription.EMPTY;
    }

    @NotNull
    public static PurchasableCosts getPurchasableCosts(@NotNull ConfigurationSection section) {
        ConfigurationSection cost_section = section.getConfigurationSection("purchasable-cost");
        if (cost_section == null) {
            PurchasableCost cost = PurchasableCost.deserialize(section.getString("purchasable-cost"));
            return cost != null ? new PurchasableCosts(cost) : PurchasableCosts.EMPTY;
        }

        Map<GameMode, PurchasableCost> costs = new HashMap<>();

        PurchasableCost default_cost = PurchasableCost.FREE;
        for (String cost_section_key : cost_section.getKeys(false)) {
            if (cost_section_key.equalsIgnoreCase("Default")) {
                default_cost = PurchasableCost.deserialize(cost_section.getString(cost_section_key), PurchasableCost.FREE);
                continue;
            }

            GameMode mode = GameMode.getByString(cost_section_key);
            if (mode == null)
                continue;

            PurchasableCost cost = PurchasableCost.deserialize(cost_section.getString(cost_section_key));
            if (cost != null)
                costs.put(mode, cost);
        }

        return !costs.isEmpty() ? new PurchasableCosts(costs, default_cost) : PurchasableCosts.EMPTY;
    }

    // Location & Region
    @NotNull
    public static List<Location> getLocationList(@NotNull ConfigurationSection config, @NotNull String path) {
        List<Location> result = new ArrayList<>();

        for (String location : config.getStringList(path)) {
            Location loc = LocationUtils.deserialize(location);
            if (loc != null)
                result.add(loc);
        }

        return result;
    }

    @Nullable
    public static Location getLocation(@NotNull ConfigurationSection config, @NotNull String path, @Nullable Location def) {
        Location result = LocationUtils.deserialize(config.getString(path));
        return result != null ? result : def;
    }

    @Nullable
    public static Location getLocation(@NotNull ConfigurationSection config, @NotNull String path) {
        return getLocation(config, path, null);
    }

    @Nullable
    public static Region getRegion(@NotNull ConfigurationSection config, @NotNull String path, @Nullable Region def) {
        Location pos1 = getLocation(config, path + ".pos-1");
        Location pos2 = getLocation(config, path + ".pos-2");

        return pos1 != null && pos2 != null ? new Region(pos1, pos2) : def;
    }

    @Nullable
    public static Region getRegion(@NotNull ConfigurationSection config, @NotNull String path) {
        return getRegion(config, path, null);
    }

    // File
    public static void save(@NotNull FileConfiguration config, @NotNull File file, @NotNull String message) {
        Preconditions.checkNotNull(config, "Cannot save a null configuration file!");
        Preconditions.checkNotNull(file, "Cannot save a configuration into a null file!");

        try {
            config.save(file);
        } catch (Exception ignored) {
            Bedwars.getBedwarsLogger().severe(message);
        }

    }

    public static void save(@NotNull String resource, @NotNull String message) {
        Preconditions.checkNotNull(resource, "Cannot save a resource with a null name!");

        try {
            Bedwars.getInstance().saveResource(resource, false);
        } catch (Exception ignored) {
            Bedwars.getBedwarsLogger().severe(message);
        }

    }

}