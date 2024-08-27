package com.slyvr.xbedwars.settings;

import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.api.game.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class BedwarsRoomSettings {

    private static final Map<GameMode, Integer> MINIMUM_PLAYERS = new HashMap<>();
    private static int countdown;

    private BedwarsRoomSettings() {
    }

    public static int getCountdown() {
        return countdown;
    }


    public static int getMinimumPlayers(@NotNull GameMode mode) {
        return mode != null ? MINIMUM_PLAYERS.getOrDefault(mode, mode.getMinPlayers()) : -1;
    }

    public static void loadSettings() {
        FileConfiguration config = XBedwars.getInstance().getConfig();

        BedwarsRoomSettings.countdown = Math.max(config.getInt("Waiting-Room-Settings.Countdown"), 5);
        BedwarsRoomSettings.loadMinimumPlayers(config);
    }

    private static void loadMinimumPlayers(@NotNull FileConfiguration config) {
        ConfigurationSection min_players_section = config.getConfigurationSection("Waiting-Room-Settings.Minimum-Players");
        if (min_players_section == null)
            return;

        for (String min_players_section_key : min_players_section.getKeys(false)) {
            GameMode mode = GameMode.getByString(min_players_section_key);
            if (mode == null)
                continue;

            int min = min_players_section.getInt(min_players_section_key);
            if (min > mode.getMaxPlayers())
                min = mode.getMaxPlayers() - mode.getTeamMax();
            else if (min < mode.getMinPlayers())
                min = mode.getMinPlayers();

            BedwarsRoomSettings.MINIMUM_PLAYERS.put(mode, min);
        }

    }

}