package com.slyvr.xbedwars.room;

import com.google.common.base.Preconditions;
import com.slyvr.xbedwars.api.room.WaitingRoom;
import com.slyvr.xbedwars.api.room.WaitingRoomUser;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class BedwarsWaitingRoomUser implements WaitingRoomUser {

    private final WaitingRoom room;
    private final Player player;


    public BedwarsWaitingRoomUser(@NotNull WaitingRoom room, @NotNull Player player) {
        Preconditions.checkNotNull(room, "WaitingRoomUser's room cannot be null!");
        Preconditions.checkNotNull(player, "WaitingRoomUser's player cannot be null!");

        this.room = room;
        this.player = player;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull WaitingRoom getWaitingRoom() {
        return room;
    }

}