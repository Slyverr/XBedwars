package com.slyvr.bedwars.room;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.arena.team.ArenaTeam;
import com.slyvr.bedwars.api.room.WaitingRoom;
import com.slyvr.bedwars.api.room.WaitingRoomTeamManager;
import com.slyvr.bedwars.api.team.TeamColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public final class BedwarsWaitingRoomTeamManager implements WaitingRoomTeamManager {

    private final Map<Player, TeamColor> players_team;
    private final WaitingRoom room;
    private final int team_max;

    public BedwarsWaitingRoomTeamManager(@NotNull WaitingRoom room) {
        Preconditions.checkNotNull(room, "Parent waiting-room cannot be null!");

        this.room = room;
        this.team_max = room.getGame().getMode().getTeamMax();

        this.players_team = new HashMap<>(room.capacity());
    }

    @Override
    public @NotNull Map<Player, TeamColor> getPlayers() {
        Map<Player, TeamColor> result = new HashMap<>(room.size());

        room.forEach(player -> result.put(player, players_team.get(player)));
        return result;
    }

    @Override
    public @NotNull Set<Player> getUnassignedPlayers() {
        Set<Player> result = new HashSet<>(room.size());

        room.forEach(player -> {
            if (!players_team.containsKey(player))
                result.add(player);
        });

        return result;
    }

    @Override
    public @NotNull Set<Player> getAssignedPlayers() {
        return new HashSet<>(players_team.keySet());
    }

    @Override
    public @NotNull Set<Player> getAssignedPlayers(@NotNull TeamColor color) {
        if (color == null)
            return new HashSet<>(0);

        Set<Player> result = new HashSet<>(team_max);

        for (Map.Entry<Player, TeamColor> entry : players_team.entrySet()) {
            if (entry.getValue() == color)
                result.add(entry.getKey());
        }

        return result;
    }

    @Override
    public @Nullable TeamColor getAssignedColor(@NotNull Player player) {
        return players_team.get(player);
    }

    @Override
    public void setAssignedColor(@NotNull Player player, @Nullable TeamColor color) {
        if (color == null || !room.contains(player) || !isReady(color) || getAssignedCount(color) >= team_max)
            return;

        this.players_team.put(player, color);
    }

    @Override
    public int getAssignedCount(@NotNull TeamColor color) {
        if (color == null)
            return 0;

        int result = 0;
        for (TeamColor assigned_color : players_team.values()) {
            if (assigned_color == color)
                result++;
        }

        return result;
    }

    @Override
    public void clear() {
        this.players_team.clear();
    }

    private boolean isReady(@NotNull TeamColor color) {
        ArenaTeam team = room.getGame().getArena().getTeam(color);
        return team != null && team.isReady();
    }

}