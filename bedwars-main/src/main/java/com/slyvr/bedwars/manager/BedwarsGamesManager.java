package com.slyvr.bedwars.manager;

import com.slyvr.bedwars.api.arena.Arena;
import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.manager.GamesManager;
import com.slyvr.bedwars.game.BedwarsGame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Predicate;

public final class BedwarsGamesManager implements GamesManager {

    private static final BedwarsGamesManager INSTANCE = new BedwarsGamesManager();

    private BedwarsGamesManager() {
    }

    @NotNull
    public static BedwarsGamesManager getInstance() {
        return INSTANCE;
    }

    @Override
    public @NotNull Collection<Game> getGames() {
        return BedwarsGame.getGames();
    }

    @Override
    public @NotNull Game create(@NotNull Arena arena, boolean prv) {
        return new BedwarsGame(arena, prv);
    }

    @Override
    public @NotNull Game create(@NotNull Arena arena) {
        return new BedwarsGame(arena);
    }

    @Override
    public @Nullable Game getGame(@NotNull Arena arena) {
        return BedwarsGame.getArenaGame(arena);
    }

    @Override
    public @Nullable Game getPlayerGame(@NotNull Player player) {
        return BedwarsGame.getPlayerGame(player);
    }

    @Override
    public @Nullable Game addPlayerToRandomGame(@NotNull Player player, @Nullable Predicate<Game> predicate) {
        return BedwarsGame.addToRandomGame(player, predicate);
    }

    @Override
    public @Nullable Game addPlayerToRandomGame(@NotNull Player player) {
        return addPlayerToRandomGame(player, null);
    }

    @Override
    public @Nullable Game getRandomGame(@Nullable Predicate<Game> predicate) {
        return BedwarsGame.randomGame(predicate);
    }

    @Override
    public @Nullable Game getRandomGame() {
        return BedwarsGame.randomGame(null);
    }

    @Override
    public boolean inGame(@NotNull Player player) {
        return BedwarsGame.inGame(player);
    }

    @Override
    public boolean inRunningGame(@NotNull Player player) {
        return BedwarsGame.inRunningGame(player);
    }

}