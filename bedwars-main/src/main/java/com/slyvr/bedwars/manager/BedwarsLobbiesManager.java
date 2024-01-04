package com.slyvr.bedwars.manager;

import com.slyvr.bedwars.api.lobby.Lobby;
import com.slyvr.bedwars.api.manager.LobbiesManager;
import com.slyvr.bedwars.configuration.LobbiesConfig;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class BedwarsLobbiesManager implements LobbiesManager {

    private static final BedwarsLobbiesManager INSTANCE = new BedwarsLobbiesManager();

    @NotNull
    public static BedwarsLobbiesManager getInstance() {
        return INSTANCE;
    }

    @Override
    public @NotNull Collection<Lobby> getLobbies() {
        return LobbiesConfig.getInstance().getLobbies();
    }

    @Override
    public @Nullable Lobby getLobby(@NotNull String name) {
        return LobbiesConfig.getInstance().getLobby(name);
    }

    @Override
    public @Nullable Lobby getRandomLobby(int places) {
        return LobbiesConfig.getInstance().getRandomLobby(places);
    }

    @Override
    public @Nullable Lobby getRandomLobby() {
        return LobbiesConfig.getInstance().getRandomLobby();
    }

    @Override
    public @Nullable Lobby sendToRandomLobby(@NotNull Player player) {
        if (player == null)
            return null;

        Lobby random = getRandomLobby(1);
        if (random != null)
            random.send(player);

        return random;
    }

}
