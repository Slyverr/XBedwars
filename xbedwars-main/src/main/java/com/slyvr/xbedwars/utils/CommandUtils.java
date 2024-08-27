package com.slyvr.xbedwars.utils;

import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.api.arena.Arena;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.game.GameMode;
import com.slyvr.xbedwars.api.game.phase.GamePhasePreset;
import com.slyvr.xbedwars.api.generator.Resource;
import com.slyvr.xbedwars.api.generator.team.TeamResourceGeneratorPreset;
import com.slyvr.xbedwars.api.generator.tiered.TieredResourceGeneratorPreset;
import com.slyvr.xbedwars.api.lang.Language;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.team.TeamColor;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.game.XBedwarsGame;
import com.slyvr.xbedwars.manager.BedwarsArenasManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Function;


public final class CommandUtils {

    private CommandUtils() {
    }


    @Nullable
    public static Arena getArena(@NotNull String name, @NotNull Player player, boolean check) {
        Arena arena = BedwarsArenasManager.getInstance().getArena(name);
        if (arena == null) {
            CommandUtils.sendMessage(Message.ARENA_MISSING, player, name);
            return null;
        }

        if (check && XBedwarsGame.isOccupied(arena)) {
            CommandUtils.sendMessage(Message.ARENA_OCCUPIED, player, name);
            return null;
        }

        return arena;
    }


    @Nullable
    public static Arena getArena(@NotNull String name, @NotNull Player player) {
        return getArena(name, player, true);
    }


    @Nullable
    public static Game getArenaGame(@NotNull Arena arena, @NotNull Player player) {
        Game game = XBedwarsGame.getArenaGame(arena);
        if (game != null)
            return game;

        CommandUtils.sendMessage(Message.ARENA_MISSING_GAME, player);
        return null;
    }


    @Nullable
    public static TeamColor getTeamColor(@NotNull String name, @NotNull Player player) {
        TeamColor color = TeamColor.getByName(name);
        if (color != null)
            return color;

        CommandUtils.sendMessage(Message.INVALID_COLOR, player);
        CommandUtils.sendInfo("/bw teams", player);
        return null;
    }


    @Nullable
    public static GameMode getGameMode(@NotNull String name, @NotNull Player player) {
        GameMode mode = GameMode.getByString(name);
        if (mode != null)
            return mode;

        CommandUtils.sendMessage(Message.INVALID_MODE, player);
        CommandUtils.sendInfo("/bw modes", player);
        return null;
    }


    @Nullable
    public static Resource getResource(@NotNull String name, @NotNull Player player) {
        Resource resource = Resource.getByName(name);
        if (resource != null && resource != Resource.FREE)
            return resource;

        CommandUtils.sendMessage(Message.INVALID_RESOURCE, player);
        CommandUtils.sendInfo("/bw resources", player);
        return null;
    }


    @Nullable
    public static Language getLanguage(@NotNull String name, @NotNull Player player) {
        Language language = Language.getByString(name);
        if (language != null)
            return language;

        CommandUtils.sendMessage(Message.INVALID_LANGUAGE, player);
        CommandUtils.sendInfo("/bw langs", player);
        return null;
    }


    @Nullable
    public static TieredResourceGeneratorPreset getTieredGeneratorPreset(@NotNull String name, @NotNull Player player) {
        TieredResourceGeneratorPreset preset = TieredResourceGeneratorPreset.getByName(name);
        if (preset != null)
            return preset;

        CommandUtils.sendMessage(Message.INVALID_PRESET_TIERED_GENERATOR, player);
        CommandUtils.sendInfo("/bw presets <resource>", player);
        return null;
    }


    @Nullable
    public static TeamResourceGeneratorPreset getTeamGeneratorPreset(@NotNull String name, @NotNull Player player) {
        TeamResourceGeneratorPreset preset = TeamResourceGeneratorPreset.getByName(name);
        if (preset != null)
            return preset;

        CommandUtils.sendMessage(Message.INVALID_PRESET_TEAM_GENERATOR, player);
        CommandUtils.sendInfo("/bw presets teams", player);
        return null;
    }


    @Nullable
    public static GamePhasePreset getGamePhasePreset(@NotNull String name, @NotNull Player player) {
        GamePhasePreset preset = GamePhasePreset.getByName(name);
        if (preset != null)
            return preset;

        CommandUtils.sendMessage(Message.INVALID_PRESET_PHASES, player);
        CommandUtils.sendInfo("/bw presets phases", player);
        return null;
    }


    @Nullable
    public static OfflinePlayer getOfflinePlayer(@NotNull String name, @NotNull Player player) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(name);
        if (target.isOnline() || target.hasPlayedBefore())
            return target;

        CommandUtils.sendMessage(Message.MISSING_PLAYER, player, target.getName());
        return null;
    }

    @NotNull
    public static <T> String format(@NotNull Iterable<T> iterable, @NotNull Function<T, String> function, @NotNull String error) {
        Iterator<T> iterator = iterable.iterator();
        if (!iterator.hasNext())
            return CommandUtils.error(error);

        StringBuilder builder = new StringBuilder(XBedwars.PREFIX);

        while (iterator.hasNext()) {
            String modified = function.apply(iterator.next());
            if (modified != null)
                builder.append(ChatColor.YELLOW).append(modified).append(ChatColor.GRAY).append(", ");
        }

        builder.deleteCharAt(builder.length() - 2);
        return builder.toString();
    }

    @NotNull
    public static <T> String format(@NotNull T[] elements, @NotNull Function<T, String> function, @NotNull String error) {
        if (elements.length == 0)
            return CommandUtils.error(error);

        StringBuilder builder = new StringBuilder(XBedwars.PREFIX);

        for (T preset : elements) {
            String modified = function.apply(preset);
            if (modified != null)
                builder.append(ChatColor.YELLOW).append(modified).append(ChatColor.GRAY).append(", ");
        }

        builder.deleteCharAt(builder.length() - 2);
        return builder.toString();
    }


    @NotNull
    public static String format(@NotNull Arena arena, @NotNull String message, @NotNull ChatColor color) {
        return XBedwars.PREFIX + ChatColor.GRAY + '(' + ChatColor.YELLOW + arena.getName() + ChatColor.GRAY + "): " + color + message;
    }

    @NotNull
    public static String format(@NotNull Arena arena, @NotNull String message) {
        return format(arena, message, ChatColor.GRAY);
    }

    @NotNull
    public static String format(@NotNull String message) {
        return XBedwars.PREFIX + ChatColor.GRAY + message;
    }

    @NotNull
    public static String success(@NotNull String message) {
        return XBedwars.PREFIX + ChatColor.GREEN + message;
    }

    @NotNull
    public static String error(@NotNull String message) {
        return XBedwars.PREFIX + ChatColor.RED + message;
    }

    @NotNull
    public static String info(@NotNull String message) {
        return XBedwars.PREFIX + ChatColor.YELLOW + message;
    }

    public static void sendArenaMessage(@NotNull Message message, @NotNull Player player, @NotNull Arena arena, Object... args) {
        player.sendMessage(CommandUtils.format(arena, MessageUtils.formatLangMessage(message, player, args)));
    }

    public static void sendMessage(@NotNull Message message, @NotNull Player player, Object... args) {
        player.sendMessage(CommandUtils.format(MessageUtils.formatLangMessage(message, player, args)));
    }

    public static void sendInfo(@NotNull String message, @NotNull Player player) {
        player.sendMessage(CommandUtils.info(message));
    }

    public static void sendUsage(@NotNull SubCommand command, @NotNull Player player) {
        player.sendMessage(CommandUtils.format(MessageUtils.formatLangMessage(Message.COMMAND_USAGE, player, command.getUsage())));
    }

}