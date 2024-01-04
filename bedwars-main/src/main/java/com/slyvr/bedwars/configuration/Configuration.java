package com.slyvr.bedwars.configuration;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.bedwars.utils.ConfigurationUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Level;


public class Configuration {

    protected FileConfiguration config;
    protected File file;

    public Configuration(@NotNull File file) {
        Preconditions.checkNotNull(file, "Configuration's file cannot be null!");

        if (file.exists() && !file.isFile())
            Bedwars.getBedwarsLogger().log(Level.SEVERE, "Configuration's file cannot be a folder! (" + file.getName() + ")");

        this.file = file;
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public Configuration(@NotNull String resource) {
        Preconditions.checkNotNull(resource, "Configuration's default resource path cannot be null!");

        this.file = new File(Bedwars.getInstance().getDataFolder(), resource);
        if (!file.exists())
            ConfigurationUtils.save(resource, "Could not save resource '" + file.getName() + "'!");

        this.config = YamlConfiguration.loadConfiguration(file);
    }

    @NotNull
    public File getFile() {
        return file;
    }

    @NotNull
    public FileConfiguration getConfig() {
        return config;
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        ConfigurationUtils.save(config, file, "Could not save configuration file '" + file.getName() + "'!");
    }

}