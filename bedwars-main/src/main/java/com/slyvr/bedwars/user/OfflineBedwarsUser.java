package com.slyvr.bedwars.user;

import com.slyvr.bedwars.Bedwars;
import com.slyvr.bedwars.api.game.GameMode;
import com.slyvr.bedwars.api.lang.Language;
import com.slyvr.bedwars.api.shop.content.Purchasable;
import com.slyvr.bedwars.api.user.OfflineUser;
import com.slyvr.bedwars.api.user.level.UserLevel;
import com.slyvr.bedwars.api.user.level.UserPrestige;
import com.slyvr.bedwars.api.user.shop.UserQuickBuy;
import com.slyvr.bedwars.api.user.stats.UserStatistic;
import com.slyvr.bedwars.api.user.stats.UserStatistics;
import com.slyvr.bedwars.api.user.wallet.UserCurrency;
import com.slyvr.bedwars.api.user.wallet.UserWallet;
import com.slyvr.bedwars.configuration.Configuration;
import com.slyvr.bedwars.configuration.shop.ItemShopConfig;
import com.slyvr.bedwars.database.Database;
import com.slyvr.bedwars.settings.BedwarsSettings;
import com.slyvr.bedwars.user.shop.BedwarsUserQuickBuy;
import com.slyvr.bedwars.utils.ConfigurationUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class OfflineBedwarsUser extends Configuration implements OfflineUser {

    protected final Map<GameMode, UserStatistics> user_stats = new LinkedHashMap<>();
    protected final Map<GameMode, UserQuickBuy> user_qbs = new LinkedHashMap<>();

    protected final OfflinePlayer owner;

    protected UserPrestige prestige;
    protected UserWallet wallet;
    protected UserLevel level;
    protected Language lang;

    public OfflineBedwarsUser(@NotNull OfflinePlayer owner) {
        super(new File(Bedwars.getInstance().getDataFolder() + "/Users", owner.getUniqueId() + ".yml"));

        this.owner = owner;

        this.loadData();
    }

    @Override
    public @NotNull OfflinePlayer getPlayer() {
        return owner;
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return owner.getUniqueId();
    }

    @Override
    public @NotNull Language getLanguage() {
        return lang;
    }

    @Override
    public void setLanguage(@NotNull Language language) {
        if (language != null)
            this.lang = language;
    }

    @Override
    public @NotNull UserLevel getLevel() {
        return level.clone();
    }

    @Override
    public void setLevel(@NotNull UserLevel level) {
        if (level != null)
            this.level = level.clone();
    }

    @Override
    public @NotNull UserPrestige getPrestige() {
        return prestige;
    }

    @Override
    public void setPrestige(@NotNull UserPrestige prestige) {
        if (prestige != null)
            this.prestige = prestige;
    }

    @Override
    public @NotNull UserStatistics getOverallStatistics() {
        UserStatistics result = new UserStatistics();

        UserStatistic[] values = UserStatistic.values();
        for (UserStatistics stat : user_stats.values()) {
            for (UserStatistic stat_type : values)
                result.incrementStatistic(stat_type, stat.getStatistic(stat_type));
        }

        return result;
    }

    @Override
    public int getOverallStatistic(@NotNull UserStatistic stat) {
        if (stat == null)
            return 0;

        int result = 0;
        for (UserStatistics stats : user_stats.values())
            result += stats.getStatistic(stat);

        return result;
    }

    @Override
    public @NotNull UserStatistics getStatistics(@NotNull GameMode mode) {
        return mode != null ? user_stats.computeIfAbsent(mode, key -> new UserStatistics()) : null;
    }

    @Override
    public @NotNull UserQuickBuy getQuickBuy(@NotNull GameMode mode) {
        return mode != null ? user_qbs.computeIfAbsent(mode, key -> new BedwarsUserQuickBuy(owner)) : null;
    }

    @Override
    public @NotNull UserWallet getWallet() {
        return wallet;
    }

    @Override
    public boolean isOnline() {
        return owner.isOnline();
    }

    @Override
    public void loadData() {
        if (Bedwars.isUserDataPersistent()) {
            Database database = Bedwars.getInstance().getDatabase();
            UUID id = owner.getUniqueId();

            for (GameMode mode : GameMode.values()) {
                this.user_stats.put(mode, database.getUserStatistics(id, mode));
                this.user_qbs.put(mode, database.getUserQuickBuy(id, mode));
            }

            this.prestige = database.getUserPrestige(id);
            this.wallet = database.getUserWallet(id);
            this.level = database.getUserLevel(id);
            this.lang = database.getUserLanguage(id);

            return;
        }

        this.reload();

        this.loadUserPreferences();
        this.loadUserProgress();
        this.loadUserWallet();

        this.loadUserStatistics();
        this.loadUserQuickBuys();
    }

    @Override
    public void saveData() {
        if (Bedwars.isUserDataPersistent() && Bedwars.getInstance().getDatabase().saveUser(this))
            return;

        FileConfiguration config = new YamlConfiguration();

        // Saving user's preferences
        ConfigurationSection preferences = config.createSection("User-Preferences");
        preferences.set("Language", lang.getName());

        // Saving user's progress
        ConfigurationSection progress = config.createSection("User-Progress");

        progress.set("Prestige", prestige.getName());
        progress.set("Level.level", level.getLevel());
        progress.set("Level.progress", level.getProgress());

        // Saving user's wallet
        ConfigurationSection wallet = config.createSection("User-Wallet");

        for (UserCurrency currency : UserCurrency.values())
            wallet.set(currency.getName(), this.wallet.getBalance(currency));

        // Saving user's statistics
        ConfigurationSection stats = config.createSection("User-Statistics");

        UserStatistic[] values = UserStatistic.values();
        for (Map.Entry<GameMode, UserStatistics> entry : user_stats.entrySet()) {
            UserStatistics user_stats = entry.getValue();
            GameMode mode = entry.getKey();

            for (UserStatistic stat : values)
                stats.set(mode.getName() + '.' + stat, user_stats.getStatistic(stat));
        }

        // Saving user's quick-buys
        ConfigurationSection quick_buys = config.createSection("User-QuickBuys");

        for (Map.Entry<GameMode, UserQuickBuy> entry : user_qbs.entrySet()) {
            UserQuickBuy quick_buy = entry.getValue();
            GameMode mode = entry.getKey();

            quick_buy.forEach((index, purchasable) -> {
                quick_buys.set(mode.getName() + ".Slot-" + index, ItemShopConfig.getPurchasablePath(purchasable));
            });
        }

        ConfigurationUtils.save(config, file, "Could not save user data for '" + owner.getUniqueId() + "'!");
    }

    private void loadUserPreferences() {
        ConfigurationSection preferences = config.getConfigurationSection("User-Preferences");
        if (preferences == null)
            preferences = config.createSection("User-Preferences");

        this.lang = Language.getByString(preferences.getString("Language"), BedwarsSettings.getDefaultlanguage());
    }

    private void loadUserProgress() {
        ConfigurationSection progress = config.getConfigurationSection("User-Progress");
        if (progress == null)
            progress = config.createSection("User-Progress");

        this.prestige = UserPrestige.getByName(progress.getString("Prestige"), Bedwars.getInstance().getDefaultPrestige());

        int level = Math.max(progress.getInt("Level.level"), 1);
        int level_progress = Math.max(progress.getInt("Level.progress"), 0);

        this.level = new UserLevel(level, level_progress, UserLevel.getLevelUpProgress(level));
    }

    private void loadUserWallet() {
        ConfigurationSection wallet = config.getConfigurationSection("User-Wallet");
        if (wallet == null) {
            this.wallet = new UserWallet();
            return;
        }

        this.wallet = new UserWallet();

        for (String currencies_section_key : wallet.getKeys(false)) {
            UserCurrency currency = UserCurrency.getByName(currencies_section_key);
            if (currency == null)
                continue;

            this.wallet.setBalance(currency, wallet.getInt(currencies_section_key));
        }

    }

    private void loadUserStatistics() {
        ConfigurationSection stats = config.getConfigurationSection("User-Statistics");
        if (stats == null)
            return;

        for (String stats_section_key : stats.getKeys(false)) {
            GameMode mode = GameMode.getByString(stats_section_key);
            if (mode == null)
                continue;

            ConfigurationSection mode_section = stats.getConfigurationSection(stats_section_key);
            if (mode_section == null)
                continue;

            UserStatistics user_stats = new UserStatistics();
            for (String mode_section_key : mode_section.getKeys(false)) {
                UserStatistic stat = UserStatistic.getByName(mode_section_key);
                if (stat == null)
                    continue;

                user_stats.setStatistic(stat, Math.max(mode_section.getInt(mode_section_key), 0));
            }

            this.user_stats.put(mode, user_stats);
        }

    }

    private void loadUserQuickBuys() {
        ConfigurationSection quick_buys = config.getConfigurationSection("User-QuickBuys");
        if (quick_buys == null)
            return;

        for (String quick_buys_key : quick_buys.getKeys(false)) {
            GameMode mode = GameMode.getByString(quick_buys_key);
            if (mode == null)
                continue;

            ConfigurationSection slots_section = quick_buys.getConfigurationSection(quick_buys_key);
            if (slots_section == null)
                continue;

            UserQuickBuy quick_buy = new BedwarsUserQuickBuy(owner);
            for (String slots_section_key : slots_section.getKeys(false)) {
                int slot = NumberConversions.toInt(slots_section_key.toLowerCase().replace("slot-", ""));
                if (slot <= 0)
                    continue;

                Purchasable purchasable = ItemShopConfig.getPathPurchasable(slots_section.getString(slots_section_key));
                if (purchasable != null)
                    quick_buy.setPurchasable(slot, purchasable);
            }

            this.user_qbs.put(mode, quick_buy);
        }

    }

}