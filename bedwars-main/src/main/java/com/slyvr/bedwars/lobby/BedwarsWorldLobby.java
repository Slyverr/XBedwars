package com.slyvr.bedwars.lobby;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.event.lobby.LobbyTeleportEvent;
import com.slyvr.bedwars.api.lobby.WorldLobby;
import com.slyvr.bedwars.game.BedwarsGame;
import com.slyvr.bedwars.manager.BedwarsUsersManager;
import com.slyvr.bedwars.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class BedwarsWorldLobby implements WorldLobby {

    private final String name;
    private final Location loc;

    private final int capacity;

    public BedwarsWorldLobby(@NotNull String name, @NotNull Location loc, int capacity) {
        Preconditions.checkNotNull(name, "Lobby's name cannot be null!");
        Preconditions.checkNotNull(loc, "Lobby's location cannot be null!");
        Preconditions.checkNotNull(loc.getWorld(), "Lobby's world cannot be null!");

        Preconditions.checkArgument(capacity >= 1, "Lobby's capacity must be at least 1!");

        this.name = name;
        this.loc = loc;

        this.capacity = capacity;
    }

    public BedwarsWorldLobby(@NotNull String name, @NotNull Location loc) {
        this(name, loc, Integer.MAX_VALUE);
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public World getWorld() {
        return loc.getWorld();
    }

    @NotNull
    @Override
    public Location getLocation() {
        return loc;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public int getPlayersCount() {
        return loc.getWorld().getPlayers().size();
    }

    @Override
    public boolean send(@NotNull Player player, boolean force) {
        if (player == null || (!force && isFull()))
            return false;

        LobbyTeleportEvent event = new LobbyTeleportEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return false;

        if (BedwarsGame.removeFromGame(player) != null)
            return true;

        if (!player.teleport(loc))
            return false;

        BedwarsUsersManager.update(player);
        MessageUtils.sendEventMessage(event.getEliminationMessageHandler(), player);
        return true;
    }

    @Override
    public boolean send(@NotNull Player player) {
        return player != null && send(player, player.getWorld().equals(loc.getWorld()));
    }

    @Override
    public boolean contains(@NotNull Player player) {
        return player != null && player.getWorld().equals(loc.getWorld());
    }

    @Override
    public boolean isFull() {
        return getPlayersCount() >= capacity;
    }

}