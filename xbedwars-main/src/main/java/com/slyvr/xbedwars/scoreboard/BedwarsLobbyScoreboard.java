package com.slyvr.xbedwars.scoreboard;

import com.slyvr.scoreboard.Scoreboard;
import com.slyvr.scoreboard.ScoreboardTitle;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.scoreboard.generic.GenericScoreboardType;
import com.slyvr.xbedwars.api.scoreboard.generic.custom.LobbyScoreboard;
import com.slyvr.xbedwars.api.user.User;
import com.slyvr.xbedwars.api.user.stats.UserStatistic;
import com.slyvr.xbedwars.api.user.wallet.UserCurrency;
import com.slyvr.xbedwars.utils.MessageUtils;
import com.slyvr.xbedwars.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BedwarsLobbyScoreboard extends AbstractScoreboard implements LobbyScoreboard {

    private static final Map<UUID, Scoreboard> BOARDS = new HashMap<>();

    static {
        for (UserStatistic stat : UserStatistic.values()) {
            LobbyScoreboardPlaceholder.register(new LobbyScoreboardPlaceholder("Statistic:" + stat.name().toLowerCase()) {

                @Override
                public @NotNull String getValue(@NotNull User user) {
                    return MessageUtils.formatLangMessage(Message.LOBBY_SCOREBOARD_STATS, user.getLanguage(), stat.getName(user.getLanguage()), NumberUtils.formatWithComma(user.getOverallStatistic(stat)));
                }
            });
        }

        LobbyScoreboardPlaceholder total_kills = new LobbyScoreboardPlaceholder("Statistic:total_kills") {

            @Override
            public @NotNull String getValue(@NotNull User user) {
                int total = user.getOverallStatistic(UserStatistic.FINAL_KILLS) + user.getOverallStatistic(UserStatistic.KILLS);
                return MessageUtils.formatLangMessage(Message.LOBBY_SCOREBOARD_STATS, user.getLanguage(), UserStatistic.KILLS.getName(user.getLanguage()), NumberUtils.formatWithComma(total));
            }
        };

        LobbyScoreboardPlaceholder total_deaths = new LobbyScoreboardPlaceholder("Statistic:total_deaths") {

            @Override
            public @NotNull String getValue(@NotNull User user) {
                int total = user.getOverallStatistic(UserStatistic.FINAL_DEATHS) + user.getOverallStatistic(UserStatistic.DEATHS);
                return MessageUtils.formatLangMessage(Message.LOBBY_SCOREBOARD_STATS, user.getLanguage(), UserStatistic.DEATHS.getName(user.getLanguage()), NumberUtils.formatWithComma(total));
            }
        };


        LobbyScoreboardPlaceholder progress_bar = new LobbyScoreboardPlaceholder("Progress:bar") {

            @Override
            public @NotNull String getValue(@NotNull User user) {
                return user.getLevel().getProgressBar(10);
            }
        };

        LobbyScoreboardPlaceholder progress = new LobbyScoreboardPlaceholder("Progress") {

            @Override
            public @NotNull String getValue(@NotNull User user) {
                return MessageUtils.formatLangMessage(Message.LOBBY_SCOREBOARD_PROGRESS, user.getLanguage(), user.getLevel().getProgressTextWithUnit());
            }
        };

        LobbyScoreboardPlaceholder level = new LobbyScoreboardPlaceholder("Level") {

            @Override
            public @NotNull String getValue(@NotNull User user) {
                return MessageUtils.formatLangMessage(Message.LOBBY_SCOREBOARD_LEVEL, user.getLanguage(), user.getPrestige().formatToScoreboard(user.getLevel()));
            }
        };

        LobbyScoreboardPlaceholder coins = new LobbyScoreboardPlaceholder("Coins") {

            @Override
            public @NotNull String getValue(@NotNull User user) {
                return MessageUtils.formatLangMessage(Message.LOBBY_SCOREBOARD_COINS, user.getLanguage(), NumberUtils.formatWithComma(user.getWallet().getBalance(UserCurrency.xbedwars_COINS)));
            }
        };

        LobbyScoreboardPlaceholder.register(total_deaths);
        LobbyScoreboardPlaceholder.register(total_kills);

        LobbyScoreboardPlaceholder.register(progress_bar);
        LobbyScoreboardPlaceholder.register(progress);
        LobbyScoreboardPlaceholder.register(level);
        LobbyScoreboardPlaceholder.register(coins);
    }

    public BedwarsLobbyScoreboard(@NotNull ScoreboardTitle title) {
        super(title);
    }

    @Override
    public void updateTitle(@NotNull User user) {
        if (user == null || !user.isOnline())
            return;

        Scoreboard board = BOARDS.get(user.getUniqueId());
        if (board != null)
            board.setNextTitle();
    }

    @Override
    public void update(@NotNull User user) {
        if (user == null || !user.isOnline() || user.getGame() != null)
            return;

        Scoreboard board = BOARDS.computeIfAbsent(user.getUniqueId(), uuid -> new Scoreboard(title));

        ScoreboardUtils.update(board, lines, GenericScoreboardType.LOBBY, user);
        board.setScoreboard(Bukkit.getPlayer(user.getUniqueId()));
    }

    public static void remove(@NotNull UUID uuid) {
        BedwarsLobbyScoreboard.BOARDS.remove(uuid);
    }

}