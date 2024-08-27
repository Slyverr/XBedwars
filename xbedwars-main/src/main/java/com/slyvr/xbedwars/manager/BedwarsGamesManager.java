package com.slyvr.xbedwars.manager;

import com.slyvr.xbedwars.api.arena.Arena;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.manager.GamesManager;
import com.slyvr.xbedwars.game.XBedwarsGame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Predicate;

public final class BedwarsGamesManager implements GamesManager {

    private static final BedwarsGamesManager INSTANCE = new BedwarsGamesManager();

    private BedwarsGamesManager() {
    }

    @Override
    public @NotNull Collection<Game> getGames() {
        return XBedwarsGame.getGames();
    }

    @Override
    public @NotNull Game create(@NotNull Arena arena, boolean prv) {
        return new XBedwarsGame(arena, prv);
    }

    @Override
    public @NotNull Game create(@NotNull Arena arena) {
        return new XBedwarsGame(arena);
    }

    @Override
    public @Nullable Game getGame(@NotNull Arena arena) {
        return XBedwarsGame.getArenaGame(arena);
    }

    @Override
    public @Nullable Game getPlayerGame(@NotNull Player player) {
        return XBedwarsGame.getPlayerGame(player);
    }

    @Override
    public @Nullable Game addPlayerToRandomGame(@NotNull Player player, @Nullable Predicate<Game> predicate) {
        return XBedwarsGame.addToRandomGame(player, predicate);
    }

    @Override
    public @Nullable Game addPlayerToRandomGame(@NotNull Player player) {
        return addPlayerToRandomGame(player, null);
    }

    @Override
    public @Nullable Game getRandomGame(@Nullable Predicate<Game> predicate) {
        return XBedwarsGame.randomGame(predicate);
    }

    @Override
    public @Nullable Game getRandomGame() {
        return XBedwarsGame.randomGame(null);
    }

    @Override
    public boolean inGame(@NotNull Player player) {
        return XBedwarsGame.inGame(player);
    }

    @Override
    public boolean inRunningGame(@NotNull Player player) {
        return XBedwarsGame.inRunningGame(player);
    }

    @NotNull
    public static BedwarsGamesManager getInstance() {
        return INSTANCE;
    }

}