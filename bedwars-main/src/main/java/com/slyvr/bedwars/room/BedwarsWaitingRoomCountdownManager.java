package com.slyvr.bedwars.room;

import com.cryptomorin.xseries.messages.Titles;
import com.google.common.base.Preconditions;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.bedwars.api.game.Game.GameState;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.api.room.WaitingRoom;
import com.slyvr.bedwars.api.room.WaitingRoomCountdownManager;
import com.slyvr.bedwars.settings.BedwarsRoomSettings;
import com.slyvr.bedwars.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;


public final class BedwarsWaitingRoomCountdownManager implements WaitingRoomCountdownManager {

    private final WaitingRoom parent;
    private BukkitTask countdown;
    private boolean waiting;
    private int time_left;


    public BedwarsWaitingRoomCountdownManager(@NotNull WaitingRoom parent) {
        Preconditions.checkNotNull(parent, "Waiting-room cannot be null!");

        this.parent = parent;
        this.waiting = true;

        this.time_left = BedwarsRoomSettings.getCountdown();
    }

    @Override
    public @NotNull WaitingRoom getWaitingRoom() {
        return parent;
    }

    @Override
    public void startCountdown() {
        if (!parent.isReady())
            return;

        this.countdown = new BukkitRunnable() {

            @Override
            public void run() {
                if (time_left == 0) {
                    BedwarsWaitingRoomCountdownManager.this.parent.getGame().start();
                    BedwarsWaitingRoomCountdownManager.this.stopCountdown();
                    return;
                }

                if (parent.getGame().isRunning()) {
                    BedwarsWaitingRoomCountdownManager.this.stopCountdown();
                    return;
                }

                BedwarsWaitingRoomCountdownManager.this.broadcastCountdown(time_left);
                BedwarsWaitingRoomCountdownManager.this.time_left--;

            }

        }.runTaskTimer(Bedwars.getInstance(), 2L, 20L);

        this.waiting = false;
        this.parent.getGame().setState(GameState.COUNTDOWN);
    }

    @Override
    public void pauseCountdown() {
        if (waiting)
            return;

        if (countdown != null)
            this.countdown.cancel();

        this.waiting = true;
        this.parent.getGame().setState(GameState.WAITING);
    }

    @Override
    public void continueCountdown() {
        if (waiting)
            this.startCountdown();
    }

    @Override
    public void stopCountdown() {
        if (countdown == null)
            return;

        this.countdown.cancel();
        this.time_left = BedwarsRoomSettings.getCountdown();

        this.waiting = true;
        this.parent.getGame().setState(GameState.WAITING);
    }

    @Override
    public long timeLeft() {
        return time_left;
    }

    @Override
    public boolean isWaiting() {
        return waiting;
    }

    private void broadcastCountdown(int time) {
        String colored = getColoredTime(time);
        if (colored == null)
            return;

        parent.forEach(player -> {
            String title = MessageUtils.formatLangMessage(Message.WAITING_ROOM_COUNTDOWN, player, colored);
            String message = MessageUtils.formatLangMessage(Message.WAITING_ROOM_COUNTDOWN, player, time);

            Titles.sendTitle(player, 10, 20, 10, title, "");
            player.sendMessage(message);
        });

    }

    @NotNull
    private String getColoredTime(int time) {
        switch (time_left) {
            case 20:
                return ChatColor.GREEN + "20";
            case 10:
                return ChatColor.GOLD + "10";
            case 5:
            case 4:
                return ChatColor.YELLOW + Integer.toString(time);
            case 3:
            case 2:
            case 1:
                return ChatColor.RED + Integer.toString(time);
            default:
                return null;
        }

    }

}