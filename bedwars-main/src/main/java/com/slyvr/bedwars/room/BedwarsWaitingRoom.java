package com.slyvr.bedwars.room;

import com.cryptomorin.xseries.messages.Titles;
import com.google.common.base.Preconditions;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.bedwars.BedwarsItems;
import com.slyvr.bedwars.api.arena.Arena;
import com.slyvr.bedwars.api.event.room.WaitingRoomJoinEvent;
import com.slyvr.bedwars.api.event.room.WaitingRoomQuitEvent;
import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.game.Game.GameState;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.api.lobby.Lobby;
import com.slyvr.bedwars.api.room.WaitingRoom;
import com.slyvr.bedwars.api.room.WaitingRoomCountdownManager;
import com.slyvr.bedwars.api.room.WaitingRoomTeamManager;
import com.slyvr.bedwars.api.room.WaitingRoomUser;
import com.slyvr.bedwars.api.scoreboard.generic.custom.WaitingRoomScoreboard;
import com.slyvr.bedwars.api.team.TeamColor;
import com.slyvr.bedwars.api.user.User;
import com.slyvr.bedwars.configuration.LobbiesConfig;
import com.slyvr.bedwars.configuration.ScoreboardsConfig;
import com.slyvr.bedwars.game.BedwarsGame;
import com.slyvr.bedwars.manager.BedwarsUsersManager;
import com.slyvr.bedwars.scoreboard.BedwarsWaitingRoomScoreboard;
import com.slyvr.bedwars.settings.BedwarsRoomSettings;
import com.slyvr.bedwars.utils.MessageUtils;
import com.slyvr.bedwars.utils.PlayerUtils;
import com.slyvr.scoreboard.ScoreboardTitle;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


public final class BedwarsWaitingRoom implements WaitingRoom {

    private final Map<Player, WaitingRoomUser> room_players;

    private final WaitingRoomCountdownManager countdown_manager;
    private final WaitingRoomTeamManager team_manager;
    private final Location spawn;
    private final Game game;
    private final int time;

    private final int game_max;
    private final int game_min;

    private WaitingRoomScoreboard scoreboard;
    private BukkitTask scoreboard_task;
    private BukkitTask title_task;

    public BedwarsWaitingRoom(@NotNull Game game) {
        Preconditions.checkNotNull(game, "Waiting-Room's game cannot be null!");

        Arena arena = game.getArena();
        com.slyvr.bedwars.api.game.GameMode mode = game.getMode();

        this.game = game;
        this.time = arena.getTime();
        this.spawn = arena.getWaitingRoomSpawnLocation();


        this.team_manager = new BedwarsWaitingRoomTeamManager(this);
        this.countdown_manager = new BedwarsWaitingRoomCountdownManager(this);

        int arena_max = arena.getReadyTeamsCount() * mode.getTeamMax();
        int arena_min = BedwarsRoomSettings.getMinimumPlayers(mode);

        this.game_max = Math.min(arena_max, mode.getMaxPlayers());
        this.game_min = Math.max(mode.getMinPlayers(), arena_min > game_max ? game_max - 1 : arena_min);

        this.scoreboard = ScoreboardsConfig.getInstance().getWaitingRoomScoreboard(mode);
        this.room_players = new ConcurrentHashMap<>(game_max);
    }

    @Override
    public @NotNull Game getGame() {
        return game;
    }

    @Override
    public @NotNull WaitingRoomCountdownManager getCountdownManager() {
        return countdown_manager;
    }

    @Override
    public @NotNull WaitingRoomTeamManager getTeamManager() {
        return team_manager;
    }

    @Override
    public @NotNull WaitingRoomScoreboard getScoreboard() {
        return scoreboard;
    }

    @Override
    public void setScoreboard(@NotNull WaitingRoomScoreboard board) {
        if (board != null)
            this.scoreboard = board;
    }

    @Override
    public boolean addPlayer(@NotNull Player player) {
        return canAddPlayer(player) && addPlayerToRoom(player, null);
    }

    @Override
    public boolean removePlayer(@NotNull Player player) {
        if (room_players.remove(player) == null)
            return false;

        this.checkCountdownAndScoreboard();
        return removeFromRoom(player, LobbiesConfig.getInstance().getRandomLobby(1));
    }

    @Override
    public boolean addPlayers(@NotNull Collection<Player> players) {
        if (!canAddPlayers(players))
            return false;

        if (!game.isPrivate())
            return addPlayersToRandomTeam(players);
        else
            return addPlayersToRoom(players);
    }

    @Override
    public boolean removePlayers(@NotNull Collection<Player> players) {
        if (players == null)
            return false;

        boolean removed_all = true;
        for (Player player : players)
            removed_all &= removePlayer(player);

        return removed_all;
    }

    @Override
    public boolean contains(@NotNull Player player) {
        return player != null && room_players.containsKey(player);
    }

    @Override
    public boolean isReady() {
        return isValidState() && room_players.size() >= game_min;
    }

    @Override
    public void broadcastMessage(@NotNull String message) {
        if (message == null)
            return;

        for (Player player : room_players.keySet())
            player.sendMessage(message);
    }

    @Override
    public void forEach(@NotNull Consumer<Player> action) {
        if (action == null)
            return;

        for (Player player : room_players.keySet())
            action.accept(player);
    }

    @Override
    public void clear() {
        if (isValidState()) {
            for (Player player : room_players.keySet())
                this.removeFromRoom(player, LobbiesConfig.getInstance().getRandomLobby());
        }

        this.room_players.clear();
        this.team_manager.clear();
        this.stopScoreboard();
    }

    @Override
    public int capacity() {
        return game_max;
    }

    @Override
    public int size() {
        return room_players.size();
    }

    private boolean canAddPlayer(@NotNull Player player) {
        if (player == null || !isValidState())
            return false;

        return room_players.size() < game_max && !room_players.containsKey(player);
    }

    private boolean canAddPlayers(@NotNull Collection<Player> players) {
        if (players == null || !isValidState())
            return false;

        return room_players.size() + players.size() <= game_max;
    }

    private boolean addPlayersToRandomTeam(@NotNull Collection<Player> players) {
        TeamColor empty_team = getRandomEmptyTeam(players.size());
        if (empty_team == null)
            return false;

        for (Player player : players) {
            if (room_players.containsKey(player)) {
                this.team_manager.setAssignedColor(player, empty_team);
                continue;
            }

            this.addPlayerToRoom(player, empty_team);
        }

        return true;
    }

    private boolean addPlayersToRoom(@NotNull Collection<Player> players) {
        boolean added_all = true;
        for (Player player : players)
            added_all &= addPlayerToRoom(player, null);

        return added_all;
    }

    private boolean addPlayerToRoom(@NotNull Player player, @Nullable TeamColor color) {
        WaitingRoomJoinEvent event = new WaitingRoomJoinEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled() || !player.teleport(spawn))
            return false;

        this.room_players.put(player, new BedwarsWaitingRoomUser(this, player));
        this.team_manager.setAssignedColor(player, color);

        BedwarsGame.removeFromGame(player);
        BedwarsGame.CONNECTED_PLAYERS_GAMES.put(player.getUniqueId(), game);

        PlayerUtils.setPlayerGameMode(player, GameMode.ADVENTURE);
        PlayerUtils.setPlayerTime(player, time, true);

        PlayerUtils.resetPlayerHealth(player);
        PlayerUtils.resetPlayerFood(player);
        PlayerUtils.clear(player);

        player.setCanPickupItems(false);

        PlayerInventory inv = player.getInventory();
        inv.setItem(8, BedwarsItems.getLobbyReturnItem(MessageUtils.getPlayerLanguage(player)));

        for (Player room_player : room_players.keySet())
            MessageUtils.sendEventMessage(event.getJoinMessageHandler(), room_player, player.getDisplayName(), room_players.size(), game_max);

        this.countdown_manager.startCountdown();
        this.startScoreboard();

        return true;
    }

    private boolean removeFromRoom(@NotNull Player player, @Nullable Lobby lobby) {
        BedwarsGame.CONNECTED_PLAYERS_GAMES.remove(player.getUniqueId());
        BedwarsWaitingRoomScoreboard.remove(player.getUniqueId());

        PlayerUtils.resetPlayerGameMode(player);
        PlayerUtils.resetPlayerHealth(player);
        PlayerUtils.resetPlayerFood(player);
        PlayerUtils.clear(player);

        User user = BedwarsUsersManager.getInstance().getUser(player);
        user.getScoreboard().update(user);

        if (lobby != null)
            lobby.send(player);

        WaitingRoomQuitEvent event = new WaitingRoomQuitEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);

        for (Player room_player : room_players.keySet())
            MessageUtils.sendEventMessage(event.getQuitMessageHandler(), room_player, player.getDisplayName());

        return true;
    }

    private boolean isValidState() {
        GameState state = game.getState();
        return state == GameState.WAITING || state == GameState.COUNTDOWN;
    }

    private void checkCountdownAndScoreboard() {
        if (room_players.size() >= game_min)
            return;

        this.stopCountdown();
        if (room_players.isEmpty())
            this.stopScoreboard();
    }

    private void startScoreboard() {
        if (scoreboard_task != null)
            return;

        this.scoreboard_task = Bukkit.getScheduler().runTaskTimer(Bedwars.getInstance(), () -> {
            for (WaitingRoomUser user : room_players.values())
                scoreboard.update(user);
        }, 0L, 20L);

        ScoreboardTitle title = scoreboard.getTitle();
        if (!title.shouldUpdate())
            return;

        this.title_task = Bukkit.getScheduler().runTaskTimerAsynchronously(Bedwars.getInstance(), () -> {
            for (WaitingRoomUser user : room_players.values())
                scoreboard.updateTitle(user);
        }, 0L, title.getUpdateTicks());
    }

    private void stopScoreboard() {
        if (scoreboard_task != null)
            this.scoreboard_task.cancel();

        if (title_task != null)
            this.title_task.cancel();

        this.scoreboard_task = null;
        this.title_task = null;
    }

    private void stopCountdown() {
        if (countdown_manager.isWaiting())
            return;

        this.countdown_manager.stopCountdown();
        for (Player player : room_players.keySet()) {
            MessageUtils.sendLangMessage(Message.WAITING_ROOM_INSUFFICIENT_PLAYERS, player);
            Titles.sendTitle(player, 10, 20, 10, MessageUtils.formatLangMessage(Message.WAITING_ROOM_INSUFFICIENT_PLAYERS_TITLE, player), "");
        }

    }

    @NotNull
    private TeamColor getRandomEmptyTeam(int size) {
        for (TeamColor color : TeamColor.values()) {
            if (team_manager.getAssignedCount(color) - size >= 0)
                return color;
        }

        return null;
    }

}