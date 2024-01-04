package com.slyvr.bedwars.settings;

import com.slyvr.bedwars.Bedwars;
import com.slyvr.bedwars.api.lang.Language;
import com.slyvr.bedwars.api.user.level.UserLevel;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;


public final class BedwarsSettings {

    private static boolean player_chat;
    private static boolean player_damage;
    private static boolean player_block_place;
    private static boolean player_block_destroy;
    private static boolean player_block_interact;
    private static boolean player_item_pickup;
    private static boolean player_item_drop;
    private static boolean entity_griefing;

    private static boolean lobby_scoreboard;
    private static boolean auto_reconnect;
    private static boolean check_updates;

    private static Language user_language;

    private BedwarsSettings() {
    }


    @NotNull
    public static Language getDefaultlanguage() {
        return user_language;
    }


    public static boolean canPlayerChat() {
        return player_chat;
    }


    public static boolean canPlayerTakeDamage() {
        return player_damage;
    }


    public static boolean canPlayerPlaceBlock() {
        return player_block_place;
    }


    public static boolean canPlayerDestroyBlock() {
        return player_block_destroy;
    }


    public static boolean canPlayerInteractWithBlock() {
        return player_block_interact;
    }


    public static boolean canPlayerPickUpItem() {
        return player_item_pickup;
    }


    public static boolean canPlayerDropItem() {
        return player_item_drop;
    }

    public static boolean canEntityGrief() {
        return entity_griefing;
    }

    public static boolean shouldUseLobbyScoreboard() {
        return lobby_scoreboard;
    }


    public static boolean shouldCheckUpdates() {
        return check_updates;
    }


    public static boolean isAutoReconnect() {
        return auto_reconnect;
    }

    public static void loadSettings() {
        FileConfiguration config = Bedwars.getInstance().getConfig();

        BedwarsSettings.player_chat = config.getBoolean("Settings.Player-Chat");
        BedwarsSettings.player_damage = config.getBoolean("Settings.Player-Damage");
        BedwarsSettings.player_block_place = config.getBoolean("Settings.Player-Block-Placing");
        BedwarsSettings.player_block_destroy = config.getBoolean("Settings.Player-Block-Destroying");
        BedwarsSettings.player_block_interact = config.getBoolean("Settings.Player-Block-Interact");
        BedwarsSettings.player_item_pickup = config.getBoolean("Settings.Player-Item-PickUp");
        BedwarsSettings.player_item_drop = config.getBoolean("Settings.Player-Item-Drop");
        BedwarsSettings.entity_griefing = config.getBoolean("Settings.Entity-Griefing");

        BedwarsSettings.lobby_scoreboard = config.getBoolean("Settings.Lobby-Scoreboard", true);
        BedwarsSettings.auto_reconnect = config.getBoolean("Settings.Auto-Reconnect", true);
        BedwarsSettings.check_updates = config.getBoolean("Settings.Check-Updates", true);

        BedwarsSettings.user_language = Language.getByString(config.getString("Settings.Default-Language"), Language.ENGLISH);

        BedwarsSettings.loadRequiredExperience(config);
    }

    private static void loadRequiredExperience(@NotNull FileConfiguration config) {
        UserLevel.setDefaultLevelUpProgress(config.getInt("Settings.Required-Exp.Default"));

        ConfigurationSection levels_section = config.getConfigurationSection("Settings.Required-Exp.Levels");
        if (levels_section == null)
            return;

        for (String levels_section_key : levels_section.getKeys(false)) {
            int level = NumberConversions.toInt(levels_section_key.replace("*", ""));
            if (level < 0)
                continue;

            int required_exp = levels_section.getInt(levels_section_key);
            if (required_exp <= 0)
                continue;

            if (levels_section_key.startsWith("*")) {
                UserLevel.setLevelUpProgressPattern(level, required_exp);
                continue;
            }

            if (level != 0) {
                UserLevel.setLevelUpProgress(level, required_exp);
                continue;
            }
        }

    }

}