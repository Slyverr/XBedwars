package com.slyvr.bedwars.manager;

import com.slyvr.bedwars.api.lang.Language;
import com.slyvr.bedwars.api.manager.UsersManager;
import com.slyvr.bedwars.api.user.OfflineUser;
import com.slyvr.bedwars.api.user.User;
import com.slyvr.bedwars.user.BedwarsUser;
import com.slyvr.bedwars.user.OfflineBedwarsUser;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class BedwarsUsersManager implements UsersManager {

    private static final BedwarsUsersManager INSTANCE = new BedwarsUsersManager();

    private static final Map<UUID, BedwarsUser> LOADED_USERS = new ConcurrentHashMap<>();
    private static final Map<UUID, OfflineBedwarsUser> OFFLINE_USERS = new ConcurrentHashMap<>();
    private static final Random RANDOM = new Random();

    private BedwarsUsersManager() {
    }

    @NotNull
    public static Language getPlayerLanguage(@NotNull Player player) {
        OfflineUser user = INSTANCE.getOfflineUser(player);
        return user != null ? user.getLanguage() : Language.ENGLISH;
    }

    @NotNull
    public static BedwarsUsersManager getInstance() {
        return INSTANCE;
    }

    public static void update(@NotNull Player player) {
        User user = INSTANCE.getUser(player);
        if (user != null)
            user.update();
    }

    public static void saveAll() {
        for (BedwarsUser user : LOADED_USERS.values())
            user.saveData();

        for (OfflineUser offline : OFFLINE_USERS.values())
            offline.saveData();
    }

    @Override
    public @NotNull Collection<User> getOnlineUsers() {
        return new HashSet<>(LOADED_USERS.values());
    }

    @Override
    public @Nullable User getUser(@NotNull Player player) {
        if (player == null || !player.isOnline())
            return null;

        BedwarsUser result = LOADED_USERS.get(player.getUniqueId());
        if (result == null)
            LOADED_USERS.put(player.getUniqueId(), result = new BedwarsUser(player));

        return result;
    }

    @Override
    public @Nullable OfflineUser getOfflineUser(@NotNull OfflinePlayer player) {
        if (player == null)
            return null;

        if (player.isOnline())
            return getUser(player.getPlayer());

        OfflineBedwarsUser result = OFFLINE_USERS.get(player.getUniqueId());
        if (result == null)
            OFFLINE_USERS.put(player.getUniqueId(), result = new OfflineBedwarsUser(player));

        return result;
    }

    @Override
    public @Nullable User getRandomUser() {
        int target = RANDOM.nextInt(LOADED_USERS.size());

        for (User user : LOADED_USERS.values()) {
            if (target-- == 0)
                return user;
        }

        return null;
    }

    @Override
    public int size() {
        return LOADED_USERS.size();
    }

}