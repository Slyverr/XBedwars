package com.slyvr.xbedwars.database;

import com.slyvr.xbedwars.api.game.GameMode;
import com.slyvr.xbedwars.api.lang.Language;
import com.slyvr.xbedwars.api.user.OfflineUser;
import com.slyvr.xbedwars.api.user.level.UserLevel;
import com.slyvr.xbedwars.api.user.level.UserPrestige;
import com.slyvr.xbedwars.api.user.shop.UserQuickBuy;
import com.slyvr.xbedwars.api.user.stats.UserStatistics;
import com.slyvr.xbedwars.api.user.wallet.UserWallet;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


public interface Database {

    @NotNull
    DatabaseType getType();

    @NotNull
    UserStatistics getUserStatistics(@NotNull UUID uuid, @NotNull GameMode mode);


    @NotNull
    UserQuickBuy getUserQuickBuy(@NotNull UUID uuid, @NotNull GameMode mode);


    @NotNull
    UserPrestige getUserPrestige(@NotNull UUID uuid);


    @NotNull
    UserLevel getUserLevel(@NotNull UUID uuid);


    @NotNull
    UserWallet getUserWallet(@NotNull UUID uuid);


    @NotNull
    Language getUserLanguage(@NotNull UUID uuid);

    boolean saveUserStatistics(@NotNull OfflineUser user, @NotNull GameMode mode);


    boolean saveUserQuickBuy(@NotNull OfflineUser user, @NotNull GameMode mode);


    boolean saveUserLevel(@NotNull OfflineUser user);


    boolean saveUserWallet(@NotNull OfflineUser user);


    boolean saveUserLanguage(@NotNull OfflineUser user);


    boolean saveUser(@NotNull OfflineUser user);

    boolean disconnect();


    boolean isConnected();

}