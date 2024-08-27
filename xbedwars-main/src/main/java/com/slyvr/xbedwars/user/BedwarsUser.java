package com.slyvr.xbedwars.user;

import com.slyvr.scoreboard.ScoreboardTitle;
import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.api.event.user.UserLanguageChangeEvent;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.lang.Language;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.scoreboard.generic.GenericScoreboardType;
import com.slyvr.xbedwars.api.scoreboard.generic.custom.LobbyScoreboard;
import com.slyvr.xbedwars.api.user.User;
import com.slyvr.xbedwars.api.user.level.UserLevel;
import com.slyvr.xbedwars.api.user.level.UserPrestige;
import com.slyvr.xbedwars.game.XBedwarsGame;
import com.slyvr.xbedwars.manager.BedwarsScoreboardsManager;
import com.slyvr.xbedwars.settings.BedwarsSettings;
import com.slyvr.xbedwars.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BedwarsUser extends OfflineBedwarsUser implements User {

    private UserPrestige display_prestige;
    private UserLevel display_level;

    private LobbyScoreboard board;
    private BukkitTask board_task;

    public BedwarsUser(@NotNull Player player) {
        super(player);

        this.setScoreboard(BedwarsScoreboardsManager.getInstance().getMainScoreboard(GenericScoreboardType.LOBBY, null));
    }

    @Override
    public @Nullable Game getGame() {
        return XBedwarsGame.getPlayerGame(owner.getPlayer());
    }

    @Override
    public boolean hasDisplayLevel() {
        return display_level != null;
    }

    @Override
    public @Nullable UserLevel getDisplayLevel() {
        return display_level != null ? display_level.clone() : null;
    }

    @Override
    public void setDisplayLevel(@Nullable UserLevel level) {
        this.display_level = level;
    }

    @Override
    public boolean hasDisplayPrestige() {
        return display_prestige != null;
    }

    @Override
    public @Nullable UserPrestige getDisplayPrestige() {
        return display_prestige;
    }

    @Override
    public void setDisplayPrestige(@Nullable UserPrestige prestige) {
        this.display_prestige = prestige;
    }

    @Override
    public @NotNull LobbyScoreboard getScoreboard() {
        return board;
    }

    @Override
    public void setScoreboard(@NotNull LobbyScoreboard board) {
        if (board == null)
            return;

        this.board = board;
        if (!BedwarsSettings.shouldUseLobbyScoreboard())
            return;

        this.board.update(this);

        ScoreboardTitle title = board.getTitle();
        if (!title.shouldUpdate())
            return;

        Bukkit.getScheduler().runTaskTimerAsynchronously(XBedwars.getInstance(), () -> board.updateTitle(this), 0L, title.getUpdateTicks());
    }

    @Override
    public void setLanguage(@NotNull Language language) {
        if (language == null)
            return;

        UserLanguageChangeEvent event = new UserLanguageChangeEvent(this, lang, (lang = language));
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void sendMessage(@NotNull Message message, Object... args) {
        this.sendMessage(MessageUtils.formatLangMessage(message, Bukkit.getPlayer(getUniqueId()), args));
    }

    @Override
    public void sendMessage(@NotNull String message) {
        if (message == null || !isOnline())
            return;

        Player player = Bukkit.getPlayer(getUniqueId());
        if (player != null)
            player.sendMessage(message);
    }

    @Override
    public void update() {
        if (BedwarsSettings.shouldUseLobbyScoreboard())
            this.board.update(this);

        this.level.setForPlayer(owner.getPlayer());
    }

}