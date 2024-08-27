package com.slyvr.xbedwars.database;

import com.google.common.base.Preconditions;
import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.api.game.GameMode;
import com.slyvr.xbedwars.api.lang.Language;
import com.slyvr.xbedwars.api.shop.content.Purchasable;
import com.slyvr.xbedwars.api.user.OfflineUser;
import com.slyvr.xbedwars.api.user.level.UserLevel;
import com.slyvr.xbedwars.api.user.level.UserPrestige;
import com.slyvr.xbedwars.api.user.shop.UserQuickBuy;
import com.slyvr.xbedwars.api.user.stats.UserStatistic;
import com.slyvr.xbedwars.api.user.stats.UserStatistics;
import com.slyvr.xbedwars.api.user.wallet.UserCurrency;
import com.slyvr.xbedwars.api.user.wallet.UserWallet;
import com.slyvr.xbedwars.configuration.shop.ItemShopConfig;
import com.slyvr.xbedwars.user.shop.BedwarsUserQuickBuy;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.UUID;


public final class MySQLDatabase implements Database {

    private final Connection connection;
    private final String username;
    private final String password;
    private final String url;

    public MySQLDatabase(@NotNull String username, @NotNull String password, @NotNull String url) {
        Preconditions.checkNotNull(username, "Database's username cannot be null!");
        Preconditions.checkNotNull(password, "Database's password cannot be null!");
        Preconditions.checkNotNull(url, "Database's url cannot be null!");

        this.username = username;
        this.password = password;
        this.url = url;

        this.connection = connect();
    }

    @Nullable
    private Connection connect() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public @NotNull DatabaseType getType() {
        return DatabaseType.MYSQL;
    }

    @Override
    public @NotNull UserStatistics getUserStatistics(@NotNull UUID uuid, @NotNull GameMode mode) {
        Preconditions.checkNotNull(uuid, "Cannot get the statistics of a null uuid!");
        Preconditions.checkNotNull(mode, "Cannot get the statistics of a null mode!");

        UserStatistics result = new UserStatistics();

        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM `user_stats` WHERE UUID = ? AND Mode = ?")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, mode.getName());

            ResultSet result_set = statement.executeQuery();
            if (!result_set.next())
                return result;

            for (UserStatistic statistic : UserStatistic.values())
                result.setStatistic(statistic, result_set.getInt(statistic.getName()));

        } catch (Exception ignored) {

        }

        return result;
    }

    @Override
    public @NotNull UserQuickBuy getUserQuickBuy(@NotNull UUID uuid, @NotNull GameMode mode) {
        Preconditions.checkNotNull(uuid, "Cannot get the statistics of a null uuid!");
        Preconditions.checkNotNull(mode, "Cannot get the statistics of a null mode!");

        UserQuickBuy result = new BedwarsUserQuickBuy(Bukkit.getOfflinePlayer(uuid));

        try (PreparedStatement statement = connection.prepareStatement("SELECT `Slot`, `Path` FROM `user_quick_buy` WHERE UUID = ? AND Mode = ?")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, mode.getName());

            ResultSet paths_set = statement.executeQuery();
            while (paths_set.next()) {
                int slot = paths_set.getInt("Slot");
                if (slot < 0 || slot > 20)
                    continue;

                Purchasable purchasable = ItemShopConfig.getPathPurchasable(paths_set.getString("Path"));
                if (purchasable != null)
                    result.setPurchasable(slot, purchasable);
            }

        } catch (Exception ignored) {

        }

        return result;
    }

    @Override
    public @NotNull UserPrestige getUserPrestige(@NotNull UUID uuid) {
        Preconditions.checkNotNull(uuid, "Cannot get the prestige of a null uuid!");

        try (PreparedStatement statement = connection.prepareStatement("SELECT `Prestige` FROM `user_level` WHERE UUID = ?")) {
            statement.setString(1, uuid.toString());

            ResultSet result_set = statement.executeQuery();
            if (result_set.next())
                return UserPrestige.getByName(result_set.getString("Prestige"), XBedwars.getInstance().getDefaultPrestige());

        } catch (Exception ignored) {

        }

        return XBedwars.getInstance().getDefaultPrestige();
    }

    @Override
    public @NotNull UserLevel getUserLevel(@NotNull UUID uuid) {
        Preconditions.checkNotNull(uuid, "Cannot get the level of a null uuid!");

        try (PreparedStatement statement = connection.prepareStatement("SELECT `Level`, `Progress` FROM `user_level` WHERE UUID = ?")) {
            statement.setString(1, uuid.toString());

            ResultSet result_set = statement.executeQuery();
            if (!result_set.next())
                return new UserLevel(1, 0);

            int level = result_set.getInt("Level");
            int progress = result_set.getInt("Progress");

            return new UserLevel(level, progress);
        } catch (Exception ignored) {

        }

        return new UserLevel(1, 0);
    }

    @Override
    public @NotNull UserWallet getUserWallet(@NotNull UUID uuid) {
        Preconditions.checkNotNull(uuid, "Cannot get the wallet of a null uuid!");

        UserWallet wallet = new UserWallet();

        try (PreparedStatement statement = connection.prepareStatement("SELECT `Currency`, `Balance` FROM `user_wallet` WHERE UUID = ?")) {
            statement.setString(1, uuid.toString());

            ResultSet result_set = statement.executeQuery();
            while (result_set.next()) {
                UserCurrency currency = UserCurrency.getByName(result_set.getString("Currency"));
                if (currency == null)
                    continue;

                int balance = result_set.getInt("Balance");
                if (balance <= 0)
                    continue;

                wallet.setBalance(currency, balance);
            }

        } catch (Exception ignored) {

        }

        return wallet;
    }

    @Override
    public @NotNull Language getUserLanguage(@NotNull UUID uuid) {
        Preconditions.checkNotNull(uuid, "Cannot get the wallet of a null uuid!");

        try (PreparedStatement statement = connection.prepareStatement("SELECT `Language` FROM `user_language` WHERE UUID = ?")) {
            statement.setString(1, uuid.toString());

            ResultSet result_set = statement.executeQuery();
            if (result_set.next())
                return Language.getByName(result_set.getString("Language"), Language.ENGLISH);

        } catch (Exception ignored) {
        }

        return Language.ENGLISH;
    }

    @Override
    public boolean saveUserStatistics(@NotNull OfflineUser user, @NotNull GameMode mode) {
        if (user == null || mode == null || !isConnected())
            return false;

        String builder = "INSERT INTO `user_stats` " + "(UUID, Mode, `Games Played`, `Beds Broken`, `Beds Lost`, Kills, Deaths, `Final Kills`, `Final Deaths`, Wins, Losses, Winstreak) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "`Games Played` = VALUES(`Games Played`), `Beds Broken` = VALUES(`Beds Broken`), `Beds Lost` = VALUES(`Beds Lost`), " +
                "`Kills` = VALUES(Kills), `Deaths` = VALUES(Deaths), `Final Kills` = VALUES(`Final Kills`), `Final Deaths` = VALUES(`Final Deaths`), " +
                "`Wins` = VALUES(`Wins`), `Losses` = VALUES(`Losses`), `Winstreak` = VALUES(`Winstreak`)";

        UserStatistics stats = user.getStatistics(mode);
        this.createUserStatisticsTable();

        try (PreparedStatement statement = connection.prepareStatement(builder)) {
            statement.setString(1, user.getUniqueId().toString());
            statement.setString(2, mode.getName());

            int index = 3;
            for (UserStatistic statistic : UserStatistic.values())
                statement.setInt(index++, stats.getStatistic(statistic));

            statement.executeUpdate();
        } catch (Exception ignored) {
            XBedwars.getBedwarsLogger().info("Could not save user '" + user.getUniqueId() + "' " + mode.getName() + " statistics!");
            return false;
        }

        return true;
    }

    @Override
    public boolean saveUserQuickBuy(@NotNull OfflineUser user, @NotNull GameMode mode) {
        if (user == null || mode == null || !isConnected())
            return false;

        String builder = "INSERT INTO `user_quick_buy` " + "(UUID, Mode, Slot, Path) " +
                "VALUES (?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "Slot = VALUES(Slot), Path = VALUES(Path)";

        UserQuickBuy quick_buy = user.getQuickBuy(mode);
        this.createUserQuickBuyTable();

        try (PreparedStatement statement = connection.prepareStatement(builder)) {
            statement.setString(1, user.getUniqueId().toString());
            statement.setString(2, mode.getName());

            for (Entry<Integer, Purchasable> entry : quick_buy.getPurchasables().entrySet()) {
                statement.setString(4, ItemShopConfig.getPurchasablePath(entry.getValue()));
                statement.setInt(3, entry.getKey());

                statement.addBatch();
            }

            statement.executeBatch();
        } catch (Exception ignored) {
            XBedwars.getBedwarsLogger().info("Could not save user '" + user.getUniqueId() + "' " + mode.getName() + " quick-buy!");
            return false;
        }

        return true;
    }

    @Override
    public boolean saveUserLevel(@NotNull OfflineUser user) {
        if (user == null || !isConnected())
            return false;


        String builder = "INSERT INTO `user_level` " + "(UUID, Level, Progress, Prestige) " +
                "VALUES (?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "Level = VALUES(Level), Progress = VALUES(Progress), Prestige = VALUES(Prestige)";

        this.createUserLevelTable();

        try (PreparedStatement statement = connection.prepareStatement(builder)) {
            statement.setString(1, user.getUniqueId().toString());

            UserLevel level = user.getLevel();
            UserPrestige prestige = user.getPrestige();

            statement.setInt(2, level.getLevel());
            statement.setInt(3, level.getProgress());
            statement.setString(4, prestige.getName());

            statement.executeUpdate();
        } catch (Exception ignored) {
            XBedwars.getBedwarsLogger().info("Could not save user '" + user.getUniqueId() + "' level!");
            return false;
        }

        return true;
    }

    @Override
    public boolean saveUserWallet(@NotNull OfflineUser user) {
        if (user == null || !isConnected())
            return false;

        String builder = "INSERT INTO `user_wallet` " + "(UUID, Currency, Balance) " +
                "VALUES (?,?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "Balance = VALUES(Balance)";

        this.createUserWalletTable();

        try (PreparedStatement statement = connection.prepareStatement(builder)) {
            statement.setString(1, user.getUniqueId().toString());

            for (Entry<UserCurrency, Integer> entry : user.getWallet().getBalances().entrySet()) {
                statement.setString(2, entry.getKey().getName());
                statement.setInt(3, entry.getValue());

                statement.addBatch();
            }

            statement.executeBatch();
        } catch (Exception ignored) {
            XBedwars.getBedwarsLogger().info("Could not save user '" + user.getUniqueId() + "' wallet!");
            return false;
        }

        return true;
    }

    @Override
    public boolean saveUserLanguage(@NotNull OfflineUser user) {
        if (user == null || !isConnected())
            return false;

        String builder = "INSERT INTO `user_language` " + "(UUID, Language) " +
                "VALUES (?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "Language = VALUES(Language)";

        this.createUserLangTable();

        try (PreparedStatement statement = connection.prepareStatement(builder)) {
            statement.setString(1, user.getUniqueId().toString());
            statement.setString(2, user.getLanguage().getName());

            statement.executeUpdate();
        } catch (Exception ignored) {
            XBedwars.getBedwarsLogger().info("Could not save user '" + user.getUniqueId() + "' language!");
            return false;
        }

        return true;
    }

    @Override
    public boolean saveUser(@NotNull OfflineUser user) {
        if (user == null || !isConnected())
            return false;

        for (GameMode mode : GameMode.values()) {
            this.saveUserStatistics(user, mode);
            this.saveUserQuickBuy(user, mode);
        }

        this.saveUserLanguage(user);
        this.saveUserWallet(user);
        this.saveUserLevel(user);

        return true;
    }

    private void createUserStatisticsTable() {
        StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS `user_stats` ");
        builder.append("(UUID VARCHAR(36), ");
        builder.append("Mode TEXT, ");

        for (UserStatistic statistic : UserStatistic.values())
            builder.append('`').append(statistic).append("` INT, ");

        builder.append("PRIMARY KEY (UUID, Mode(255)))");

        this.executeUpdate(builder.toString());
    }

    private void createUserQuickBuyTable() {
        String builder = "CREATE TABLE IF NOT EXISTS `user_quick_buy` " + "(UUID VARCHAR(36), " +
                "Mode TEXT, " +
                "Slot INT, " +
                "Path TEXT, " +
                "PRIMARY KEY (UUID, Mode, Slot))";

        this.executeUpdate(builder);
    }

    private void createUserLevelTable() {
        String builder = "CREATE TABLE IF NOT EXISTS `user_level` " + "(UUID VARCHAR(36), " +
                "Level INT, " +
                "Progress INT, " +
                "Prestige TEXT, " +
                "PRIMARY KEY (UUID))";

        this.executeUpdate(builder);
    }

    private void createUserWalletTable() {
        String builder = "CREATE TABLE IF NOT EXISTS `user_wallet` " + "(UUID VARCHAR(36), " +
                "Currency TEXT, " +
                "Balance INT, " +
                "PRIMARY KEY (UUID, Currency(255)))";

        this.executeUpdate(builder);
    }

    private void createUserLangTable() {
        String builder = "CREATE TABLE IF NOT EXISTS `user_language` " + "(UUID VARCHAR(36), " +
                "Language TEXT, " +
                "PRIMARY KEY (UUID))";

        this.executeUpdate(builder);
    }

    @Override
    public boolean disconnect() {
        if (XBedwars.getInstance().isEnabled())
            return false;

        try {
            this.connection.close();
        } catch (Exception ignored) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isConnected() {
        try {
            return !connection.isClosed();
        } catch (Exception ignored) {
            return false;
        }
    }

    private int executeUpdate(@NotNull String sql, Object... params) {
        try (PreparedStatement statement = prepareStatement(sql, params)) {
            return statement.executeUpdate();
        } catch (SQLException ignored) {
        }

        return 0;
    }

    @Nullable
    private PreparedStatement prepareStatement(@NotNull String sql, Object... args) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);

        for (int i = 0; i < args.length; i++)
            statement.setObject(i + 1, args[i]);

        return statement;
    }

}