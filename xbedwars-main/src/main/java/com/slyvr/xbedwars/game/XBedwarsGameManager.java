package com.slyvr.xbedwars.game;

import com.google.common.base.Preconditions;
import com.slyvr.scoreboard.ScoreboardTitle;
import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.api.arena.Arena;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.game.GameManager;
import com.slyvr.xbedwars.api.game.phase.GamePhase;
import com.slyvr.xbedwars.api.game.phase.GamePhasePreset;
import com.slyvr.xbedwars.api.lang.Language;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.api.reward.GameRewardReason;
import com.slyvr.xbedwars.api.reward.GameRewardType;
import com.slyvr.xbedwars.api.scoreboard.generic.GenericScoreboardType;
import com.slyvr.xbedwars.api.scoreboard.generic.custom.GameScoreboard;
import com.slyvr.xbedwars.api.team.GameTeam;
import com.slyvr.xbedwars.api.team.TeamColor;
import com.slyvr.xbedwars.api.trap.Trap;
import com.slyvr.xbedwars.api.user.User;
import com.slyvr.xbedwars.api.user.stats.UserStatistic;
import com.slyvr.xbedwars.api.user.stats.UserStatistics;
import com.slyvr.xbedwars.manager.BedwarsScoreboardsManager;
import com.slyvr.xbedwars.manager.BedwarsUsersManager;
import com.slyvr.xbedwars.settings.BedwarsGameSettings;
import com.slyvr.xbedwars.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.slyvr.xbedwars.api.game.Game.GameState;


public final class XBedwarsGameManager implements GameManager {

    private final Set<UUID> trap_safe = new HashSet<>();

    private final GamePhasePreset preset;
    private final Game game;

    private GameScoreboard scoreboard;
    private BukkitTask title_task;
    private BukkitTask board_task;

    private int current_phase_index;
    private int next_phase_time;
    private int current_time;
    private int length;

    public XBedwarsGameManager(@NotNull Game game) {
        Preconditions.checkNotNull(game, "Cannot manage a null game!");

        this.game = game;
        this.preset = game.getArena().getPhasesPreset();

        this.scoreboard = BedwarsScoreboardsManager.getInstance().getMainScoreboard(GenericScoreboardType.GAME, game.getMode());
    }

    @Override
    public @NotNull Game getGame() {
        return game;
    }

    @Override
    public boolean start() {
        if (game.getState() != GameState.STARTING)
            return false;

        game.forEach(player -> {
            User user = BedwarsUsersManager.getInstance().getUser(player.getPlayer());
            if (user == null)
                return;

            UserStatistics stats = user.getStatistics(game.getMode());
            if (stats != null)
                stats.incrementStatistic(UserStatistic.GAMES_PLAYED, 1);
        });

        this.current_phase_index = -1;
        this.next_phase_time = 0;

        this.board_task = Bukkit.getScheduler().runTaskTimer(XBedwars.getInstance(), () -> {
            if (XBedwarsGameManager.this.next_phase_time == 0) {
                if (current_phase_index >= 0 && current_phase_index < preset.size())
                    XBedwarsGameManager.this.preset.getPhase(current_phase_index).apply(game);

                if (current_phase_index + 1 >= preset.size()) {
                    XBedwarsGameManager.this.game.stop();
                    return;
                }

                XBedwarsGameManager.this.current_phase_index++;
                XBedwarsGameManager.this.next_phase_time = preset.getPhase(current_phase_index).getTimeToWait();
            }

            XBedwarsGameManager.this.current_time++;
            XBedwarsGameManager.this.next_phase_time--;

            XBedwarsGameManager.this.updateGamePlayers();
            XBedwarsGameManager.this.updateGameTeams();
        }, 0L, 20L);

        if (scoreboard == null)
            return true;

        ScoreboardTitle title = scoreboard.getTitle();
        if (!title.shouldUpdate())
            return true;

        this.title_task = Bukkit.getScheduler().runTaskTimerAsynchronously(XBedwars.getInstance(), () -> {
            game.forEach(scoreboard::updateTitle);
        }, 0L, title.getUpdateTicks());

        return true;
    }

    @Override
    public boolean stop() {
        if (game.getState() != GameState.ENDING)
            return false;

        if (board_task != null)
            this.board_task.cancel();

        if (title_task != null)
            this.title_task.cancel();

        this.trap_safe.clear();
        return true;
    }

    private void updateGamePlayers() {
        if (current_time % BedwarsGameSettings.getTimeToPlayForReward() != 0) {
            for (GamePlayer game_player : game.getGamePlayers()) {
                if (!game.isDisconnected(game_player.getPlayer()))
                    scoreboard.update(game_player);
            }

            return;
        }

        GameRewardType[] rewards_types = GameRewardType.values();
        for (GamePlayer game_player : game.getGamePlayers()) {
            Player player = game_player.getPlayer();
            if (game.isDisconnected(player))
                continue;

            scoreboard.update(game_player);
            if (game.isEliminated(player))
                continue;

            for (GameRewardType type : rewards_types) {
                int amount = BedwarsGameSettings.getRewardAmount(type, GameRewardReason.TIME_PLAYED, game.getMode());
                if (amount <= 0)
                    continue;

                game_player.getRewardManager().increment(type, GameRewardReason.TIME_PLAYED, amount);
                player.sendMessage(getRewardMessage(type, MessageUtils.getPlayerLanguage(player), amount));
            }
        }

    }

    private void updateGameTeams() {
        Collection<GameTeam> teams = game.getGameTeams();
        Collection<GamePlayer> players = game.getGamePlayers();

        Arena arena = game.getArena();
        for (GameTeam team : teams) {
            TeamColor team_color = team.getColor();
            Location team_spawn = arena.getTeam(team_color).getSpawnLocation();
            if (team_spawn == null)
                continue;

            List<Trap> traps = team.getTrapManager().getTraps();
            if (traps.isEmpty())
                continue;

            for (GamePlayer gp : players) {
                if (gp.getTeamColor() == team_color)
                    continue;

                Player player = gp.getPlayer();
                if (trap_safe.contains(player.getUniqueId()) || game.isSpectator(player))
                    continue;

                if (player.getLocation().distanceSquared(team_spawn) > 400)
                    continue;

                for (Trap trap : traps) {
                    if (!onTrigger(gp, trap, team_color))
                        continue;

                    team.getTrapManager().removeTrap(trap);
                    break;
                }
            }
        }

    }

    private boolean onTrigger(@NotNull GamePlayer gp, @NotNull Trap trap, @NotNull TeamColor color) {
        if (!trap.onTrigger(gp, color))
            return false;

        // TODO: Usage of gp.setTrapSafe
        UUID uuid = gp.getPlayer().getUniqueId();

        this.trap_safe.add(uuid);
        Bukkit.getScheduler().runTaskLaterAsynchronously(XBedwars.getInstance(), () ->
                        trap_safe.remove(uuid),
                20L * trap.getDuration());
        return true;
    }

    @Override
    public @NotNull GameScoreboard getScoreboard() {
        return scoreboard;
    }

    @Override
    public void setScoreboard(@NotNull GameScoreboard board) {
        if (board != null)
            this.scoreboard = board;
    }

    @Override
    public @Nullable GamePhase getPreviousPhase() {
        return game.isRunning() ? preset.getPhase(current_phase_index - 1) : null;
    }

    @Override
    public @Nullable GamePhase getCurrentPhase() {
        return game.isRunning() ? preset.getPhase(current_phase_index) : null;
    }

    @Override
    public @Nullable GamePhase getNextPhase() {
        return game.isRunning() ? preset.getPhase(current_phase_index + 1) : null;
    }

    @Override
    public long getGameLength() {
        return length;
    }

    @Override
    public long getCurrentTime() {
        return current_time;
    }

    @Override
    public long getTimeLeft() {
        return length - current_time;
    }

    @Override
    public long getTimeLeftForNextPhase() {
        return next_phase_time;
    }

    @NotNull
    private String getRewardMessage(@NotNull GameRewardType type, @NotNull Language lang, int amount) {
        return String.valueOf(type.getColor()) + '+' + amount + ' ' + type.getName() + "! " + GameRewardReason.TIME_PLAYED.getReason(lang);
    }

}
