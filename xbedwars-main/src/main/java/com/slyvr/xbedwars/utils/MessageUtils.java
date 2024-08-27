package com.slyvr.xbedwars.utils;

import com.slyvr.xbedwars.api.event.EventMessageHandler;
import com.slyvr.xbedwars.api.lang.Language;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.manager.BedwarsUsersManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class MessageUtils {

    private MessageUtils() {
    }


    @NotNull
    public static Language getPlayerLanguage(@NotNull Player player) {
        return BedwarsUsersManager.getPlayerLanguage(player);
    }


    @Nullable
    public static String formatEventMessage(@NotNull EventMessageHandler handler, @NotNull Language lang, Object... args) {
        return handler.format(lang, args);
    }


    @Nullable
    public static String formatEventMessage(@NotNull EventMessageHandler handler, @NotNull Player player, Object... args) {
        return formatEventMessage(handler, BedwarsUsersManager.getPlayerLanguage(player), args);
    }


    @Nullable
    public static String formatLangMessage(@NotNull Message message, @NotNull Language lang, Object... args) {
        return lang != null ? Message.format(lang.getMessagePattern(message), args) : null;
    }


    @Nullable
    public static String formatLangMessage(@NotNull Message message, @NotNull Player player, Object... args) {
        return formatLangMessage(message, BedwarsUsersManager.getPlayerLanguage(player), args);
    }


    @Nullable
    public static String formatLangCustomMessage(@NotNull String key, @NotNull Language lang, Object... args) {
        return lang != null ? Message.format(lang.getCustomMessagePattern(key), args) : null;
    }


    @Nullable
    public static String formatLangCustomMessage(@NotNull String key, @NotNull Player player, Object... args) {
        return formatLangCustomMessage(key, BedwarsUsersManager.getPlayerLanguage(player), args);
    }


    public static void sendEventMessage(@NotNull EventMessageHandler handler, @NotNull Player player, Object... args) {
        String formatted = formatEventMessage(handler, player, args);
        if (formatted != null)
            player.sendMessage(formatted);
    }


    public static void sendLangMessage(@NotNull Message message, @NotNull Player player, Object... args) {
        String formatted = formatLangMessage(message, player, args);
        if (formatted != null)
            player.sendMessage(formatted);
    }

}
