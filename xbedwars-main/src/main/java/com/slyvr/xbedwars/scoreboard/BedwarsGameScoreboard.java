package com.slyvr.xbedwars.scoreboard;

import com.slyvr.scoreboard.Scoreboard;
import com.slyvr.scoreboard.ScoreboardTitle;
import com.slyvr.xbedwars.api.game.GameManager;
import com.slyvr.xbedwars.api.game.phase.GamePhase;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.api.player.GamePlayerStatisticManager.GamePlayerStatistic;
import com.slyvr.xbedwars.api.scoreboard.generic.GenericScoreboardType;
import com.slyvr.xbedwars.api.scoreboard.generic.custom.GameScoreboard;
import com.slyvr.xbedwars.api.team.TeamColor;
import com.slyvr.xbedwars.utils.MessageUtils;
import com.slyvr.xbedwars.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BedwarsGameScoreboard extends AbstractScoreboard implements GameScoreboard {

    private static final Map<UUID, Scoreboard> BOARDS = new HashMap<>();

    static {
        for (TeamColor color : TeamColor.values()) {
            GameScoreboardPlaceholder.register(new GameScoreboardPlaceholder("Team:" + color + ":status") {

                @Override
                public @NotNull String getValue(@NotNull GamePlayer player) {
                    return ScoreboardUtils.getTeamStatus(player, color);
                }
            });
        }

        for (GamePlayerStatistic stat : GamePlayerStatistic.values()) {
            if (stat == GamePlayerStatistic.BEDS_LOST || stat == GamePlayerStatistic.FINAL_DEATHS)
                continue;

            GameScoreboardPlaceholder.register(new GameScoreboardPlaceholder("Statistic:" + stat.name().toLowerCase()) {

                @Override
                public @NotNull String getValue(@NotNull GamePlayer player) {
                    return MessageUtils.formatLangMessage(Message.GAME_SCOREBOARD_STATISTIC, player.getPlayer(), stat.getName(), NumberUtils.formatWithComma(player.getStatisticManager().getStatistic(stat)));
                }
            });
        }

        GameScoreboardPlaceholder.register(new GameScoreboardPlaceholder("Phase") {

            @Override
            public @NotNull String getValue(@NotNull GamePlayer player) {
                GameManager manager = player.getGame().getManager();
                GamePhase current = manager.getCurrentPhase();

                return MessageUtils.formatLangMessage(Message.GAME_SCOREBOARD_PHASE, player.getPlayer(), current.getName(), format(manager));
            }

            @NotNull
            private String format(@NotNull GameManager manager) {
                long time = manager.getTimeLeftForNextPhase();

                long minute = time / 60;
                long seconds = time % 60;

                StringBuilder result = new StringBuilder().append(minute).append(':');
                if (seconds < 10)
                    result.append('0');

                return result.append(seconds).toString();
            }

        });

    }

    public BedwarsGameScoreboard(@NotNull ScoreboardTitle title) {
        super(title);
    }

    @Override
    public void updateTitle(@NotNull GamePlayer player) {
        if (player == null || !player.getPlayer().isOnline())
            return;

        Scoreboard board = BOARDS.get(player.getPlayer().getUniqueId());
        if (board != null)
            board.setNextTitle();
    }

    @Override
    public void update(@NotNull GamePlayer player) {
        if (player == null || !player.getPlayer().isOnline())
            return;

        Scoreboard board = BOARDS.computeIfAbsent(player.getPlayer().getUniqueId(), uuid -> new Scoreboard(title));
        ScoreboardUtils.update(board, lines, GenericScoreboardType.GAME, player);

        board.setScoreboard(Bukkit.getPlayer(player.getPlayer().getUniqueId()));
    }

    public static void remove(@NotNull UUID uuid) {
        BOARDS.remove(uuid);
    }

}