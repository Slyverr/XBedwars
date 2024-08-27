package com.slyvr.xbedwars.manager;

import com.google.common.base.Preconditions;
import com.slyvr.xbedwars.api.arena.Arena;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.manager.ArenasManager;
import com.slyvr.xbedwars.arena.BedwarsArena;
import com.slyvr.xbedwars.game.XBedwarsGame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

public final class BedwarsArenasManager implements ArenasManager {

    private static final BedwarsArenasManager INSTANCE = new BedwarsArenasManager();

    private static final Map<String, Arena> LOADED_ARENAS = new HashMap<>();
    private static final Random RANDOM = new Random();

    private BedwarsArenasManager() {
    }

    @Override
    public @NotNull Collection<Arena> getArenas() {
        return new HashSet<>(LOADED_ARENAS.values());
    }

    @Override
    public @NotNull Collection<Arena> getReadyArenas() {
        Set<Arena> result = new HashSet<>(LOADED_ARENAS.size());

        for (Arena arena : LOADED_ARENAS.values()) {
            if (arena.isReady())
                result.add(arena);
        }

        return result;
    }

    @Override
    public @Nullable Arena getRandomArena() {
        return getRandomArena(null);
    }

    @Override
    public @Nullable Arena getRandomArena(@Nullable Predicate<Arena> predicate) {
        List<Arena> arenas = new ArrayList<>();

        for (Arena arena : LOADED_ARENAS.values()) {
            if (predicate == null || predicate.test(arena))
                arenas.add(arena);
        }

        return !arenas.isEmpty() ? arenas.get(RANDOM.nextInt(arenas.size())) : null;
    }

    @Override
    public @Nullable Arena getArena(@NotNull String name) {
        return name != null ? LOADED_ARENAS.get(name.toLowerCase()) : null;
    }

    @Override
    public @NotNull Arena create(@NotNull String name) {
        Preconditions.checkNotNull(name, "Arena's name cannot be null!");

        return LOADED_ARENAS.computeIfAbsent(name.toLowerCase(), k -> new BedwarsArena(name));
    }

    @Override
    public boolean remove(@NotNull String name) {
        if (name == null)
            return false;

        Arena arena = LOADED_ARENAS.remove(name.toLowerCase());
        if (arena == null)
            return false;

        Game game = XBedwarsGame.getArenaGame(arena);
        if (game != null)
            game.stop(true);

        File file = ((BedwarsArena) arena).getFile();
        return !file.exists() || file.delete();
    }

    @Override
    public boolean isOccupied(@NotNull Arena arena) {
        return XBedwarsGame.isOccupied(arena);
    }

    public static void saveAll() {
        for (Arena arena : LOADED_ARENAS.values())
            arena.saveData();
    }

    public static BedwarsArenasManager getInstance() {
        return INSTANCE;
    }

}