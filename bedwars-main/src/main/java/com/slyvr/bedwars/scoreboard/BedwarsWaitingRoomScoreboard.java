package com.slyvr.bedwars.scoreboard;

import com.slyvr.bedwars.Bedwars;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.api.room.WaitingRoom;
import com.slyvr.bedwars.api.room.WaitingRoomCountdownManager;
import com.slyvr.bedwars.api.room.WaitingRoomUser;
import com.slyvr.bedwars.api.scoreboard.generic.GenericScoreboardType;
import com.slyvr.bedwars.api.scoreboard.generic.custom.WaitingRoomScoreboard;
import com.slyvr.bedwars.utils.MessageUtils;
import com.slyvr.scoreboard.Scoreboard;
import com.slyvr.scoreboard.ScoreboardTitle;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BedwarsWaitingRoomScoreboard extends AbstractScoreboard implements WaitingRoomScoreboard {

    private static final Map<UUID, Scoreboard> BOARDS = new HashMap<>();

    static {
        WaitingRoomScoreboardPlaceholder waiting_status = new WaitingRoomScoreboardPlaceholder("Waiting_Status") {

            @Override
            public @NotNull String getValue(@NotNull WaitingRoomUser user) {
                WaitingRoomCountdownManager manager = user.getWaitingRoom().getCountdownManager();
                if (!manager.isWaiting())
                    return MessageUtils.formatLangMessage(Message.WAITING_ROOM_SCOREBOARD_COUNTDOWN, user.getPlayer(), manager.timeLeft());
                else
                    return MessageUtils.formatLangMessage(Message.WAITING_ROOM_SCOREBOARD_WAITING, user.getPlayer());
            }
        };

        WaitingRoomScoreboardPlaceholder players_status = new WaitingRoomScoreboardPlaceholder("Players_Status") {

            @Override
            public @NotNull String getValue(@NotNull WaitingRoomUser user) {
                WaitingRoom room = user.getWaitingRoom();
                return MessageUtils.formatLangMessage(Message.WAITING_ROOM_SCOREBOARD_PLAYERS_STATUS, user.getPlayer(), room.size(), room.capacity());
            }
        };

        WaitingRoomScoreboardPlaceholder version = new WaitingRoomScoreboardPlaceholder("Version") {

            private final String version = Bedwars.getInstance().getDescription().getVersion();

            @Override
            public @NotNull String getValue(@NotNull WaitingRoomUser user) {
                return MessageUtils.formatLangMessage(Message.WAITING_ROOM_SCOREBOARD_VERSION, user.getPlayer(), version);
            }
        };

        WaitingRoomScoreboardPlaceholder mode_comp = new WaitingRoomScoreboardPlaceholder("Mode:comp") {

            @Override
            public @NotNull String getValue(@NotNull WaitingRoomUser user) {
                return MessageUtils.formatLangMessage(Message.WAITING_ROOM_SCOREBOARD_MODE, user.getPlayer(), user.getWaitingRoom().getGame().getMode().getTeamsComposition());
            }
        };

        WaitingRoomScoreboardPlaceholder mode = new WaitingRoomScoreboardPlaceholder("Mode") {

            @Override
            public @NotNull String getValue(@NotNull WaitingRoomUser user) {
                return MessageUtils.formatLangMessage(Message.WAITING_ROOM_SCOREBOARD_MODE, user.getPlayer(), user.getWaitingRoom().getGame().getMode().getName());
            }
        };

        WaitingRoomScoreboardPlaceholder map = new WaitingRoomScoreboardPlaceholder("Map") {

            @Override
            public @NotNull String getValue(@NotNull WaitingRoomUser user) {
                String name = user.getWaitingRoom().getGame().getArena().getMapName();
                if (name != null)
                    return MessageUtils.formatLangMessage(Message.WAITING_ROOM_SCOREBOARD_MAP, user.getPlayer(), name);
                else
                    return MessageUtils.formatLangMessage(Message.UNDEFINED, user.getPlayer());
            }
        };

        WaitingRoomScoreboardPlaceholder.register(waiting_status);
        WaitingRoomScoreboardPlaceholder.register(players_status);
        WaitingRoomScoreboardPlaceholder.register(version);

        WaitingRoomScoreboardPlaceholder.register(mode_comp);
        WaitingRoomScoreboardPlaceholder.register(mode);
        WaitingRoomScoreboardPlaceholder.register(map);
    }

    public BedwarsWaitingRoomScoreboard(@NotNull ScoreboardTitle title) {
        super(title);
    }

    public static void remove(@NotNull UUID uuid) {
        BOARDS.remove(uuid);
    }

    @Override
    public void updateTitle(@NotNull WaitingRoomUser user) {
        if (user == null || !user.getPlayer().isOnline())
            return;

        Scoreboard board = BOARDS.get(user.getPlayer().getUniqueId());
        if (board != null)
            board.setNextTitle();
    }

    @Override
    public void update(@NotNull WaitingRoomUser user) {
        if (user == null || !user.getPlayer().isOnline())
            return;

        Scoreboard board = BOARDS.computeIfAbsent(user.getPlayer().getUniqueId(), game -> new Scoreboard(title));

        ScoreboardUtils.update(board, lines, GenericScoreboardType.WAITING_ROOM, user);
        board.setScoreboard(Bukkit.getPlayer(user.getPlayer().getUniqueId()));
    }

}