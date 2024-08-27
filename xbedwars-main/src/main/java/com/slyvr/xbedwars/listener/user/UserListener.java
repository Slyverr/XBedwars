package com.slyvr.xbedwars.listener.user;

import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.api.event.user.AsyncUserChatEvent;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.user.User;
import com.slyvr.xbedwars.api.user.level.UserLevel;
import com.slyvr.xbedwars.api.user.level.UserPrestige;
import com.slyvr.xbedwars.game.XBedwarsGame;
import com.slyvr.xbedwars.manager.BedwarsLobbiesManager;
import com.slyvr.xbedwars.manager.BedwarsUsersManager;
import com.slyvr.xbedwars.scoreboard.BedwarsLobbyScoreboard;
import com.slyvr.xbedwars.settings.BedwarsSettings;
import com.slyvr.xbedwars.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;


public final class UserListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onUserChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (XBedwarsGame.inGame(player))
            return;

        if (!BedwarsSettings.canPlayerChat() && !player.hasPermission("bw.flags.chat")) {
            event.setCancelled(true);
            return;
        }

        User user = BedwarsUsersManager.getInstance().getUser(player);

        UserLevel level = user.hasDisplayLevel() ? user.getDisplayLevel() : user.getLevel();
        UserPrestige prestige = user.hasDisplayPrestige() ? user.getDisplayPrestige() : user.getPrestige();

        AsyncUserChatEvent bwEvent = new AsyncUserChatEvent(user, event.getMessage());
        Bukkit.getPluginManager().callEvent(bwEvent);

        if (bwEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        String format = bwEvent.getFormat();
        format = format.replace("{level}", prestige.formatToChat(level));
        format = format.replace("{player}", player.getDisplayName());
        format = format.replace("{message}", event.getMessage());

        event.setFormat(format);
    }

    @EventHandler
    public void onUserJoin(PlayerJoinEvent event) {
        UserListener.this.loadUser(event.getPlayer());

        if (BedwarsSettings.shouldCheckUpdates())
            UserListener.this.checkUpdate(event.getPlayer());
    }

    @EventHandler
    public void onUserQuit(PlayerQuitEvent event) {
        User user = BedwarsUsersManager.getInstance().getUser(event.getPlayer());

        Bukkit.getScheduler().runTaskAsynchronously(XBedwars.getInstance(), () -> {
            BedwarsLobbyScoreboard.remove(user.getUniqueId());
            user.saveData();
        });
    }

    @EventHandler
    public void onUserLevelChange(PlayerLevelChangeEvent event) {
        this.loadUser(event.getPlayer());
    }

    @EventHandler
    public void onUserExpChange(PlayerExpChangeEvent event) {
        this.loadUser(event.getPlayer());
    }

    @EventHandler
    public void onUserRespawn(PlayerRespawnEvent event) {
        this.loadUser(event.getPlayer());
    }

    private void loadUser(@NotNull Player player) {
        User user = BedwarsUsersManager.getInstance().getUser(player);
        user.update();

        if (XBedwarsGame.getDisconnectedPlayerGame(player) != null)
            BedwarsLobbiesManager.getInstance().sendToRandomLobby(player);
    }

    private void checkUpdate(@NotNull Player player) {
        if (!player.hasPermission("bw.update"))
            return;

        try (InputStream stream = new URL("https://api.spigotmc.org/legacy/update.php?resource=114242").openStream(); Scanner scanner = new Scanner(stream)) {
            String scanned_version = scanner.next();
            String current_version = XBedwars.getInstance().getDescription().getVersion().replace(".", "");

            if (NumberConversions.toInt(scanned_version.replace(".", "")) > NumberConversions.toInt(current_version))
                player.sendMessage(XBedwars.PREFIX + MessageUtils.formatLangMessage(Message.UPDATE, player, scanned_version));

        } catch (Exception ignored) {
        }

    }

}