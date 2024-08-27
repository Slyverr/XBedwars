package com.slyvr.xbedwars.scoreboard;

import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.api.scoreboard.generic.GenericScoreboardPlaceholder;
import com.slyvr.xbedwars.api.scoreboard.generic.GenericScoreboardType;
import com.slyvr.xbedwars.api.team.TeamColor;
import com.slyvr.xbedwars.utils.MessageUtils;
import com.slyvr.scoreboard.Scoreboard;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

import static com.slyvr.xbedwars.api.game.Game.GameState;


public final class ScoreboardUtils {

    private static final String CROSS_MARK = ChatColor.RED + "✘";
    private static final String CHECK_MARK = ChatColor.GREEN + "✓";

    private ScoreboardUtils() {
    }

    @NotNull
    public static String getTeamStatus(@NotNull GamePlayer player, @NotNull TeamColor color) {
        Message message = player.getTeamColor() == color ? Message.GAME_SCOREBOARD_STATUS_PLAYER_TEAM : Message.GAME_SCOREBOARD_STATUS_OTHER_TEAM;
        return getTeamStatus(message, player, player.getGame(), color);
    }

    public static <T> void update(@NotNull Scoreboard board, @NotNull String[] lines, @NotNull GenericScoreboardType<?, T> type, @NotNull T obj) {
        for (int i = 0; i < 15; i++) {
            String line = lines[i];
            if (line == null)
                continue;

            if (!line.trim().isEmpty())
                board.setText(i + 1, GenericScoreboardPlaceholder.replaceAll(type, line, obj));
            else
                board.setText(i + 1, "");
        }

    }

    @NotNull
    private static String getTeamStatus(@NotNull Message message, @NotNull GamePlayer player, @NotNull Game game, @NotNull TeamColor color) {
        if (game.getState() == GameState.ENDED || game.isEliminated(color))
            return MessageUtils.formatLangMessage(message, player.getPlayer(), color.getColoredRepresentingChar(), format(color, player), CROSS_MARK);

        if (game.hasBed(color))
            return MessageUtils.formatLangMessage(message, player.getPlayer(), color.getColoredRepresentingChar(), format(color, player), CHECK_MARK);

        AtomicInteger players_left = new AtomicInteger();
        game.forEach(game_player -> {
            if (game_player.getTeamColor() == color)
                players_left.getAndIncrement();
        });

        String status = players_left.get() > 0 ? ChatColor.GREEN + players_left.toString() : CROSS_MARK;
        return MessageUtils.formatLangMessage(message, player.getPlayer(), color.getColoredRepresentingChar(), format(color, player), status);
    }

    @NotNull
    private static String format(@NotNull TeamColor color, @NotNull GamePlayer player) {
        return MessageUtils.formatLangMessage(Message.valueOf("TEAM_COLOR_" + color.name()), player.getPlayer());
    }

}