package com.slyvr.xbedwars.manager;

import com.google.common.base.Preconditions;
import com.slyvr.scoreboard.ScoreboardTitle;
import com.slyvr.xbedwars.api.game.GameMode;
import com.slyvr.xbedwars.api.manager.ScoreboardsManager;
import com.slyvr.xbedwars.api.scoreboard.generic.GenericScoreboardType;
import com.slyvr.xbedwars.configuration.ScoreboardsConfig;
import com.slyvr.xbedwars.scoreboard.BedwarsGameScoreboard;
import com.slyvr.xbedwars.scoreboard.BedwarsLobbyScoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BedwarsScoreboardsManager implements ScoreboardsManager {

    private static final BedwarsScoreboardsManager INSTANCE = new BedwarsScoreboardsManager();

    private BedwarsScoreboardsManager() {
    }

    @Override
    public <T> @NotNull T getMainScoreboard(@NotNull GenericScoreboardType<T, ?> type, @Nullable GameMode mode) {
        Preconditions.checkNotNull(type, "Scoreboard's type cannot be null!");

        if (type == GenericScoreboardType.LOBBY)
            return (T) ScoreboardsConfig.getInstance().getLobbyScoreboard();

        if (type == GenericScoreboardType.GAME)
            return (T) ScoreboardsConfig.getInstance().getGameScoreboard(mode);

        if (type == GenericScoreboardType.WAITING_ROOM)
            return (T) ScoreboardsConfig.getInstance().getWaitingRoomScoreboard(mode);

        return null;
    }

    @Override
    public <T> @NotNull T getMainScoreboard(@NotNull GenericScoreboardType<T, ?> type) {
        return getMainScoreboard(type, null);
    }

    @Override
    public <T> @NotNull T getNewScoreboard(@NotNull GenericScoreboardType<T, ?> type, @NotNull ScoreboardTitle title) {
        Preconditions.checkNotNull(type, "Scoreboard's type cannot be null!");
        Preconditions.checkNotNull(title, "Scoreboard's title cannot be null!");

        if (type == GenericScoreboardType.LOBBY)
            return (T) new BedwarsLobbyScoreboard(title);

        if (type == GenericScoreboardType.GAME)
            return (T) new BedwarsGameScoreboard(title);

        if (type == GenericScoreboardType.WAITING_ROOM)
            return (T) new BedwarsLobbyScoreboard(title);

        return null;
    }

    @Override
    public <T> @NotNull T getNewScoreboard(@NotNull GenericScoreboardType<T, ?> type) {
        return getNewScoreboard(type, ScoreboardsConfig.DEFAULT_TITLE);
    }

    public static BedwarsScoreboardsManager getInstance() {
        return INSTANCE;
    }

}