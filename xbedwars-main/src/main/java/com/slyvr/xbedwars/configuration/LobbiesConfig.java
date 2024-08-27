package com.slyvr.xbedwars.configuration;

import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.api.lobby.Lobby;
import com.slyvr.xbedwars.api.lobby.WorldLobby;
import com.slyvr.xbedwars.lobby.BedwarsWorldLobby;
import com.slyvr.xbedwars.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


public final class LobbiesConfig extends Configuration {

    private static final LobbiesConfig INSTANCE = new LobbiesConfig();

    private final Map<String, Lobby> lobbies = new HashMap<>();
    private boolean isLoaded;

    private LobbiesConfig() {
        super(new File(XBedwars.getInstance().getDataFolder(), "Lobbies.yml"));
    }

    @NotNull
    public Collection<Lobby> getLobbies() {
        return Collections.unmodifiableCollection(lobbies.values());
    }

    @Nullable
    public Lobby getLobby(@NotNull String name) {
        return name != null ? lobbies.get(name.toLowerCase()) : null;
    }

    public void setLobby(@NotNull Lobby lobby) {
        if (lobby != null)
            this.lobbies.put(lobby.getName().toLowerCase(), lobby);
    }

    @Nullable
    public Lobby getRandomLobby(int places) {
        if (lobbies.isEmpty())
            return null;

        for (Lobby lobby : lobbies.values()) {
            if (lobby.getPlayersCount() + places <= lobby.getCapacity())
                return lobby;
        }

        return null;
    }

    @Nullable
    public Lobby getRandomLobby() {
        if (lobbies.isEmpty())
            return null;

        int index = ThreadLocalRandom.current().nextInt(0, lobbies.size());
        for (Lobby lobby : lobbies.values()) {
            if (index-- == 0)
                return lobby;
        }

        return null;
    }

    public boolean remove(@NotNull String name) {
        return name != null && lobbies.remove(name.toLowerCase()) != null;
    }

    public void saveLobbies() {
        this.config = new YamlConfiguration();

        for (Lobby lobby : lobbies.values()) {
            if (lobby instanceof WorldLobby)
                this.save(config.createSection("World-Lobbies." + lobby.getName()), (WorldLobby) lobby);
        }

        this.save();
    }

    public void loadLobbies() {
        if (isLoaded)
            return;

        ConfigurationSection world_lobbies_section = config.getConfigurationSection("World-Lobbies");
        if (world_lobbies_section == null) {
            this.isLoaded = true;
            return;
        }

        for (String lobby_name : world_lobbies_section.getKeys(false)) {
            ConfigurationSection lobby_section = world_lobbies_section.getConfigurationSection(lobby_name);
            if (lobby_section == null)
                continue;

            Location location = LocationUtils.deserialize(lobby_section.getString("location"));
            if (location == null)
                continue;

            int capacity = lobby_section.getInt("capacity");
            if (capacity <= 0)
                capacity = Integer.MAX_VALUE;

            this.lobbies.put(lobby_name.toLowerCase(), new BedwarsWorldLobby(lobby_name, location, capacity));
        }

        this.isLoaded = true;
    }

    private void save(@NotNull ConfigurationSection section, @NotNull WorldLobby lobby) {
        section.set("location", LocationUtils.serialize(lobby.getLocation(), true));
        section.set("capacity", lobby.getCapacity());
    }

    @NotNull
    public static LobbiesConfig getInstance() {
        return INSTANCE;
    }

}