package com.slyvr.bedwars.configuration;

import com.slyvr.bedwars.Bedwars;
import com.slyvr.bedwars.api.game.GameMode;
import com.slyvr.bedwars.api.scoreboard.Scoreboard;
import com.slyvr.bedwars.api.scoreboard.generic.GenericScoreboard;
import com.slyvr.bedwars.api.scoreboard.generic.GenericScoreboardType;
import com.slyvr.bedwars.api.scoreboard.generic.custom.GameScoreboard;
import com.slyvr.bedwars.api.scoreboard.generic.custom.LobbyScoreboard;
import com.slyvr.bedwars.api.scoreboard.generic.custom.WaitingRoomScoreboard;
import com.slyvr.bedwars.scoreboard.BedwarsGameScoreboard;
import com.slyvr.bedwars.scoreboard.BedwarsLobbyScoreboard;
import com.slyvr.bedwars.scoreboard.BedwarsWaitingRoomScoreboard;
import com.slyvr.bedwars.utils.ChatUtils;
import com.slyvr.scoreboard.ScoreboardTitle;
import com.slyvr.scoreboard.utils.ScoreboardUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class ScoreboardsConfig extends Configuration {

    public static final ScoreboardTitle DEFAULT_TITLE;

    private static final ScoreboardsConfig INSTANCE;

    static {
        String yellow_bedwars = ChatUtils.bold("BED WARS", ChatColor.YELLOW);
        String white_bedwars = ChatUtils.bold("BED WARS", ChatColor.WHITE);

        List<String> titles = Arrays.asList(
                white_bedwars, white_bedwars,
                white_bedwars, white_bedwars,
                white_bedwars, white_bedwars,
                white_bedwars, white_bedwars,
                white_bedwars, white_bedwars,
                white_bedwars, white_bedwars,
                white_bedwars, white_bedwars,
                white_bedwars, white_bedwars,
                white_bedwars, white_bedwars,
                white_bedwars, white_bedwars,

                ChatUtils.bold("", ChatColor.WHITE) + ChatUtils.bold("B", ChatColor.GOLD) + ChatUtils.bold("ED WARS", ChatColor.YELLOW),
                ChatUtils.bold("B", ChatColor.WHITE) + ChatUtils.bold("E", ChatColor.GOLD) + ChatUtils.bold("D WARS", ChatColor.YELLOW),
                ChatUtils.bold("BE", ChatColor.WHITE) + ChatUtils.bold("D", ChatColor.GOLD) + ChatUtils.bold(" WARS", ChatColor.YELLOW),
                ChatUtils.bold("BED ", ChatColor.WHITE) + ChatUtils.bold("W", ChatColor.GOLD) + ChatUtils.bold("ARS", ChatColor.YELLOW),
                ChatUtils.bold("BED W", ChatColor.WHITE) + ChatUtils.bold("A", ChatColor.GOLD) + ChatUtils.bold("RS", ChatColor.YELLOW),
                ChatUtils.bold("BED WA", ChatColor.WHITE) + ChatUtils.bold("R", ChatColor.GOLD) + ChatUtils.bold("S", ChatColor.YELLOW),
                ChatUtils.bold("BED WAR", ChatColor.WHITE) + ChatUtils.bold("S", ChatColor.GOLD) + ChatUtils.bold("", ChatColor.YELLOW),

                white_bedwars, white_bedwars,
                yellow_bedwars, yellow_bedwars,
                white_bedwars, white_bedwars,
                yellow_bedwars, yellow_bedwars
        );

        DEFAULT_TITLE = new ScoreboardTitle(titles, 5);
        INSTANCE = new ScoreboardsConfig();
    }

    private final Map<String, ScoreboardTitle> titles = new HashMap<>();
    private final Map<GameMode, GameScoreboard> game_boards = new HashMap<>();
    private final Map<GameMode, WaitingRoomScoreboard> waiting_boards = new HashMap<>();

    private LobbyScoreboard default_lobby_scoreboard;
    private WaitingRoomScoreboard default_waiting_scoreboard;
    private GameScoreboard default_game_scoreboard;

    private ScoreboardsConfig() {
        super("Scoreboards.yml");

        this.setDefaultResource();

        this.loadDefaultTitles();
        this.loadDefaultScoreboards();

        this.loadScoreboards(GenericScoreboardType.WAITING_ROOM, "Waiting-Room-Scoreboards");
        this.loadScoreboards(GenericScoreboardType.GAME, "Game-Scoreboards");
    }

    @NotNull
    public static ScoreboardsConfig getInstance() {
        return INSTANCE;
    }

    @NotNull
    public LobbyScoreboard getLobbyScoreboard() {
        return default_lobby_scoreboard;
    }

    @NotNull
    public WaitingRoomScoreboard getWaitingRoomScoreboard(@NotNull GameMode mode) {
        return waiting_boards.getOrDefault(mode, default_waiting_scoreboard);
    }

    @NotNull
    public GameScoreboard getGameScoreboard(@NotNull GameMode mode) {
        return game_boards.getOrDefault(mode, default_game_scoreboard);
    }

    private void loadDefaultTitles() {
        ConfigurationSection titles_section = config.getConfigurationSection("Scoreboards-Titles");
        if (titles_section == null)
            return;

        for (String title_name : titles_section.getKeys(false)) {
            ScoreboardTitle title = getScoreboardTitle(titles_section.getConfigurationSection(title_name));
            if (title != null)
                this.titles.put(title_name.toLowerCase(), title);
        }

    }

    private void loadDefaultScoreboards() {
        ConfigurationSection lobby_section = getDefaultScoreboardSection("Scoreboards.Lobby-Scoreboard");
        ConfigurationSection waiting_section = getDefaultScoreboardSection("Scoreboards.Waiting-Room-Scoreboards.Default");
        ConfigurationSection game_section = getDefaultScoreboardSection("Scoreboards.Game-Scoreboards.Default");

        this.default_waiting_scoreboard = new BedwarsWaitingRoomScoreboard(getScoreboardTitle(waiting_section.getString("Title")));
        this.default_lobby_scoreboard = new BedwarsLobbyScoreboard(getScoreboardTitle(lobby_section.getString("Title")));
        this.default_game_scoreboard = new BedwarsGameScoreboard(getScoreboardTitle(game_section.getString("Title")));

        this.setLines(default_waiting_scoreboard, waiting_section);
        this.setLines(default_lobby_scoreboard, lobby_section);
        this.setLines(default_game_scoreboard, game_section);
    }

    private <T> void loadScoreboards(@NotNull GenericScoreboardType<T, ?> type, @NotNull String path) {
        ConfigurationSection game_section = getDefaultScoreboardSection("Scoreboards." + path);

        for (String scoreboard_key : game_section.getKeys(false)) {
            if (scoreboard_key.equalsIgnoreCase("Default"))
                continue;

            ConfigurationSection scoreboard_section = game_section.getConfigurationSection(scoreboard_key);
            if (scoreboard_section == null)
                continue;

            List<String> modes = scoreboard_section.getStringList("Modes");
            if (modes.isEmpty())
                continue;

            GenericScoreboard scoreboard = getScoreboard(type, getScoreboardTitle(scoreboard_section.getString("Title")));
            this.setLines(scoreboard, scoreboard_section);

            for (String mode_name : modes) {
                GameMode mode = GameMode.getByString(mode_name);
                if (mode == null)
                    continue;

                if (type == GenericScoreboardType.WAITING_ROOM)
                    this.waiting_boards.put(mode, (WaitingRoomScoreboard) scoreboard);
                else if (type == GenericScoreboardType.GAME)
                    this.game_boards.put(mode, (GameScoreboard) scoreboard);
            }

        }

    }

    @NotNull
    private GenericScoreboard getScoreboard(@NotNull GenericScoreboardType<?, ?> type, @NotNull ScoreboardTitle title) {
        if (type == GenericScoreboardType.LOBBY)
            return new BedwarsLobbyScoreboard(title);

        if (type == GenericScoreboardType.WAITING_ROOM)
            return new BedwarsWaitingRoomScoreboard(title);

        if (type == GenericScoreboardType.GAME)
            return new BedwarsGameScoreboard(title);

        return null;
    }

    @NotNull
    private ConfigurationSection getDefaultScoreboardSection(@NotNull String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        return section == null ? config.getDefaults().getConfigurationSection(path) : section;
    }

    @NotNull
    private ScoreboardTitle getScoreboardTitle(@NotNull ConfigurationSection section) {
        if (section == null)
            return DEFAULT_TITLE;

        List<String> titles = section.getStringList("titles");
        if (titles.isEmpty())
            return DEFAULT_TITLE;

        titles.replaceAll(title -> formatTitle(title, ScoreboardUtils.MAXIMUM_SCOREBOARD_TITLE_LENGTH));
        return new ScoreboardTitle(titles, section.getLong("update-ticks"));
    }

    @NotNull
    private ScoreboardTitle getScoreboardTitle(@NotNull String name) {
        return name != null ? titles.getOrDefault(name.toLowerCase(), DEFAULT_TITLE) : DEFAULT_TITLE;
    }

    @NotNull
    private String formatTitle(@NotNull String title, int maximum) {
        return ChatUtils.format(title.length() > maximum ? title.substring(0, maximum) : title);
    }

    private void setLines(@NotNull Scoreboard board, @NotNull ConfigurationSection section) {
        board.setLines(section.getStringList("Lines"));
    }

    private void setDefaultResource() {
        InputStream stream = Bedwars.getInstance().getResource("Scoreboards.yml");
        if (stream == null)
            return;

        this.config.options().copyDefaults(true);
        this.config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(stream)));
    }

}