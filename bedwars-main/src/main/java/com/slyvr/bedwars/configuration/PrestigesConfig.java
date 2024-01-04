package com.slyvr.bedwars.configuration;

import com.slyvr.bedwars.api.user.level.UserPrestige;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class PrestigesConfig extends Configuration {

    private static final PrestigesConfig INSTANCE = new PrestigesConfig();

    private UserPrestige default_prestige;
    private boolean isLoaded;

    private PrestigesConfig() {
        super("Prestiges.yml");

        this.default_prestige = UserPrestige.DEFAULT;
    }

    @NotNull
    public static PrestigesConfig getInstance() {
        return INSTANCE;
    }

    @NotNull
    public UserPrestige getDefaultPrestige() {
        return default_prestige;
    }

    public void loadPrestiges() {
        if (isLoaded)
            return;

        ConfigurationSection prestiges_section = config.getConfigurationSection("Prestiges");
        if (prestiges_section == null)
            return;

        for (String prestige_name : prestiges_section.getKeys(false)) {
            try {
                UserPrestige.register(loadPrestige(prestige_name));
            } catch (Exception ignored) {
                // Ignoring the exception
            }
        }

        this.default_prestige = UserPrestige.getByName(config.getString("Default"), UserPrestige.DEFAULT);
        this.isLoaded = true;
    }

    @Nullable
    private UserPrestige loadPrestige(@NotNull String name) {
        ConfigurationSection prestige_section = config.getConfigurationSection("Prestiges." + name);
        if (prestige_section == null)
            return null;

        String display_name = prestige_section.getString("prestige-name");
        if (display_name == null)
            return null;

        String chat_format = prestige_section.getString("prestige-formats.chat");
        if (chat_format == null)
            return null;

        String board_format = prestige_section.getString("prestige-formats.scoreboard");
        if (board_format == null)
            return null;

        String[] data = prestige_section.getString("prestige-range", "").split("-");
        if (data.length < 2)
            return null;

        int start = NumberConversions.toInt(data[0]);
        int end = NumberConversions.toInt(data[1]);

        return start > 0 && end >= start ? new UserPrestige(name, display_name, chat_format, board_format, start, end) : null;
    }

}