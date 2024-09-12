package com.slyvr.xbedwars;

import com.slyvr.xbedwars.api.XBedwarsPlugin;
import com.slyvr.xbedwars.api.XBedwarsUtils;
import com.slyvr.xbedwars.api.game.GameMode;
import com.slyvr.xbedwars.api.game.phase.GamePhase;
import com.slyvr.xbedwars.api.generator.Resource;
import com.slyvr.xbedwars.api.manager.ArenasManager;
import com.slyvr.xbedwars.api.manager.GamesManager;
import com.slyvr.xbedwars.api.manager.LobbiesManager;
import com.slyvr.xbedwars.api.manager.ScoreboardsManager;
import com.slyvr.xbedwars.api.manager.TrapsManager;
import com.slyvr.xbedwars.api.manager.UpgradesManager;
import com.slyvr.xbedwars.api.manager.UsersManager;
import com.slyvr.xbedwars.api.trap.Trap;
import com.slyvr.xbedwars.api.user.User;
import com.slyvr.xbedwars.api.user.level.UserPrestige;
import com.slyvr.xbedwars.arena.BedwarsArena;
import com.slyvr.xbedwars.commands.BedwarsCommand;
import com.slyvr.xbedwars.commands.PlayCommand;
import com.slyvr.xbedwars.commands.RejoinCommand;
import com.slyvr.xbedwars.commands.ShoutCommand;
import com.slyvr.xbedwars.configuration.LobbiesConfig;
import com.slyvr.xbedwars.configuration.PresetsConfig;
import com.slyvr.xbedwars.configuration.PrestigesConfig;
import com.slyvr.xbedwars.configuration.shop.ItemShopConfig;
import com.slyvr.xbedwars.configuration.shop.UpgradeShopConfig;
import com.slyvr.xbedwars.database.Database;
import com.slyvr.xbedwars.database.DatabaseType;
import com.slyvr.xbedwars.database.MySQLDatabase;
import com.slyvr.xbedwars.game.XBedwarsGame;
import com.slyvr.xbedwars.game.phase.BedBreakPhase;
import com.slyvr.xbedwars.game.phase.ResourceGeneratorUpgradePhase;
import com.slyvr.xbedwars.game.phase.SuddenDeathPhase;
import com.slyvr.xbedwars.listener.arena.ArenaListener;
import com.slyvr.xbedwars.listener.boss.GameBossListener;
import com.slyvr.xbedwars.listener.entity.GameEntityListener;
import com.slyvr.xbedwars.listener.game.GameWaitingRoomListener;
import com.slyvr.xbedwars.listener.hologram.HologramListener;
import com.slyvr.xbedwars.listener.mechanics.GameMechanicsListener;
import com.slyvr.xbedwars.listener.npc.ShopNPCListener;
import com.slyvr.xbedwars.listener.player.GamePlayerBlockListener;
import com.slyvr.xbedwars.listener.player.GamePlayerChatListener;
import com.slyvr.xbedwars.listener.player.GamePlayerConnectionListener;
import com.slyvr.xbedwars.listener.player.GamePlayerDamageListener;
import com.slyvr.xbedwars.listener.player.GamePlayerInteractListener;
import com.slyvr.xbedwars.listener.player.GamePlayerItemListener;
import com.slyvr.xbedwars.listener.player.GamePlayerListener;
import com.slyvr.xbedwars.listener.shop.ShopListener;
import com.slyvr.xbedwars.listener.user.UserListener;
import com.slyvr.xbedwars.manager.BedwarsArenasManager;
import com.slyvr.xbedwars.manager.BedwarsGamesManager;
import com.slyvr.xbedwars.manager.BedwarsLobbiesManager;
import com.slyvr.xbedwars.manager.BedwarsScoreboardsManager;
import com.slyvr.xbedwars.manager.BedwarsTrapsManager;
import com.slyvr.xbedwars.manager.BedwarsUpgradesManager;
import com.slyvr.xbedwars.manager.BedwarsUsersManager;
import com.slyvr.xbedwars.settings.BedwarsGameSettings;
import com.slyvr.xbedwars.settings.BedwarsRoomSettings;
import com.slyvr.xbedwars.settings.BedwarsSettings;
import com.slyvr.xbedwars.trap.AlarmTrap;
import com.slyvr.xbedwars.trap.BlindnessTrap;
import com.slyvr.xbedwars.trap.CounterOffensiveTrap;
import com.slyvr.xbedwars.trap.MiningFatigueTrap;
import com.slyvr.xbedwars.upgrade.custom.BossBuffUpgrade;
import com.slyvr.xbedwars.upgrade.custom.EnchantmentUpgrade;
import com.slyvr.xbedwars.upgrade.custom.HealPoolUpgrade;
import com.slyvr.xbedwars.upgrade.custom.tiered.ForgeUpgrade;
import com.slyvr.xbedwars.upgrade.custom.tiered.TieredEffectUpgrade;
import com.slyvr.xbedwars.upgrade.custom.tiered.TieredEnchantmentUpgrade;
import com.slyvr.xbedwars.utils.ChatUtils;
import com.slyvr.xbedwars.utils.Version;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitWorker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public final class XBedwars extends JavaPlugin implements XBedwarsPlugin {

    public static final String PREFIX = ChatUtils.format("&6[&bX&bBedwars&6]&r ");

    private static List<GamePhase> default_phases;
    private static Database database;
    private static XBedwars instance;
    private static Version version;
    private static Logger logger;

    private static boolean user_data;

    private XBedwarsUtils utils;
    private boolean disable;

    @Override
    public void onLoad() {
        if (Version.getVersion() == Version.UNSUPPORTED) {
            this.getLogger().info("Unsupported version detected: " + Version.getVersionName());
            this.disable = true;
            return;
        }

        try {
            Class.forName("org.spigotmc.SpigotConfig");
        } catch (ClassNotFoundException e) {
            this.getLogger().info("Unsupported implementation detected!");
            // this.getLogger().warning("Unsupported server implementation detected! Spigot-based server required.");
            this.disable = true;
            return;
        }

        XBedwars.instance = this;
    }

    @Override
    public void onEnable() {
        if (disable) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        XBedwars.version = Version.getVersion();
        XBedwars.logger = getLogger();

        this.saveDefaultConfig();
        this.initMetrics();

        logger.info("Registering commands!");
        this.registerCommands();

        logger.info("Registering listeners!");
        this.registerListeners();

        logger.info("Registering game-phases!");
        this.registerGamePhases();

        logger.info("Registering upgrades & traps!");
        this.registerUpgrades();
        this.registerTraps();

        logger.info("Registering utilities!");

        try {
            this.registerUtilities();
        } catch (Exception e) {
            logger.severe("Could not register utilities (" + version + ")");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        logger.info("Loading settings!");
        BedwarsSettings.loadSettings();
        BedwarsRoomSettings.loadSettings();
        BedwarsGameSettings.loadSettings();

        logger.info("Loading database info!");
        this.loadDatabase();

        logger.info("Loading prestiges!");
        this.loadPrestiges();

        logger.info("Loading presets!");
        this.loadPresets();

        logger.info("Loading lobbies!");
        this.loadLobbies();

        logger.info("Loading arenas!");
        this.loadArenas();

        logger.info("Loading shops!");
        this.loadShops();

        logger.info("Loading users!");
        this.loadUsers();

        logger.info("Detected NMS Version: " + version);
        logger.info("XBedwars has been enabled!");
    }

    @Override
    public void onDisable() {
        ConsoleCommandSender console = Bukkit.getConsoleSender();

        for (BukkitWorker worker : Bukkit.getScheduler().getActiveWorkers()) {
            if (!worker.getThread().isAlive())
                worker.getThread().start();
        }

        logger.info("Disabling XBedwars");

        logger.info("Shutting down running games!");
        XBedwarsGame.stopAll(true);

        logger.info("Saving loaded arenas!");
        BedwarsArenasManager.saveAll();

        logger.info("Saving loaded users!");
        BedwarsUsersManager.saveAll();

        logger.info("Saving lobbies!");
        LobbiesConfig.getInstance().saveLobbies();

        if (database != null)
            database.disconnect();

        logger.info("XBedwars has been disabled!");
    }

    private void initMetrics() {
        Metrics metrics = new Metrics(this, 17596);

        metrics.addCustomChart(new AdvancedPie("popular_modes", () -> {
            Map<String, Integer> result = new HashMap<>(4);
            result.put("Solo", XBedwarsGame.getPlayersCount(GameMode.SOLO));
            result.put("Doubles", XBedwarsGame.getPlayersCount(GameMode.DUO));
            result.put("3v3v3v3", XBedwarsGame.getPlayersCount(GameMode.TRIO));
            result.put("4v4v4v4", XBedwarsGame.getPlayersCount(GameMode.QUAD));

            return result;
        }));

    }

    private void registerCommands() {
        this.getCommand("Bedwars").setExecutor(new BedwarsCommand());
        this.getCommand("Rejoin").setExecutor(new RejoinCommand());
        this.getCommand("Shout").setExecutor(new ShoutCommand());
        this.getCommand("Play").setExecutor(new PlayCommand());
    }

    private void registerListeners() {
        PluginManager manager = Bukkit.getPluginManager();

        manager.registerEvents(new ArenaListener(), this);

        manager.registerEvents(new GameBossListener(), this);
        manager.registerEvents(new GameEntityListener(), this);
        manager.registerEvents(new GameMechanicsListener(), this);

        manager.registerEvents(new GameWaitingRoomListener(), this);

        manager.registerEvents(new GamePlayerConnectionListener(), this);
        manager.registerEvents(new GamePlayerInteractListener(), this);
        manager.registerEvents(new GamePlayerDamageListener(), this);
        manager.registerEvents(new GamePlayerBlockListener(), this);
        manager.registerEvents(new GamePlayerChatListener(), this);
        manager.registerEvents(new GamePlayerItemListener(), this);
        manager.registerEvents(new GamePlayerListener(), this);

        manager.registerEvents(new HologramListener(), this);
        manager.registerEvents(new ShopNPCListener(), this);

        manager.registerEvents(new ShopListener(), this);
        manager.registerEvents(new UserListener(), this);

        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new XBungeeMessageListener());
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        Bukkit.getMessenger().registerIncomingPluginChannel(this, "slyvr:xbedwars", new XBedwarsMessageListener());
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "slyvr:xbedwars");
    }

    private void registerUpgrades() {
        UpgradesManager manager = BedwarsUpgradesManager.getInstance();

        manager.registerUpgrade("Sharpness", EnchantmentUpgrade.SHARPNESS_UPGRADE);

        manager.registerUpgrade("Boss Buff", BossBuffUpgrade.BOSS_BUFF_UPGRADE);
        manager.registerUpgrade("Boss_Buff", BossBuffUpgrade.BOSS_BUFF_UPGRADE);

        manager.registerUpgrade("Heal Pool", HealPoolUpgrade.HEAL_POOL_UPGRADE);
        manager.registerUpgrade("Heal_Pool", HealPoolUpgrade.HEAL_POOL_UPGRADE);

        // Tiered upgrades
        manager.registerUpgrade("Protection", TieredEnchantmentUpgrade.PROTECTION_UPGRADE);
        manager.registerUpgrade("Forge", ForgeUpgrade.FORGE_UPGRADE);

        manager.registerUpgrade("Maniac Miner", TieredEffectUpgrade.MANIAC_MINER_UPGRADE);
        manager.registerUpgrade("Maniac_Miner", TieredEffectUpgrade.MANIAC_MINER_UPGRADE);
    }

    private void registerTraps() {
        TrapsManager manager = BedwarsTrapsManager.getInstance();

        manager.registerTrap("Alarm", new AlarmTrap(10));

        Trap counter_offensive_trap = new CounterOffensiveTrap(10);
        manager.registerTrap("Counter_Offensive", counter_offensive_trap);
        manager.registerTrap("Counter-Offensive", counter_offensive_trap);
        manager.registerTrap("Counter Offensive", counter_offensive_trap);

        manager.registerTrap("Blindness", new BlindnessTrap(8));

        Trap fatigue = new MiningFatigueTrap(10);
        manager.registerTrap("Mining Fatigue", fatigue);
        manager.registerTrap("Mining_Fatigue", fatigue);
    }

    private void registerGamePhases() {
        // Diamond Generators upgrade phases
        GamePhase diamond_one = new ResourceGeneratorUpgradePhase(Resource.DIAMOND, "II", 60 * 6, 2);
        GamePhase diamond_two = new ResourceGeneratorUpgradePhase(Resource.DIAMOND, "III", 60 * 6, 3);

        GamePhase.register(diamond_one);
        GamePhase.register(diamond_two);

        // Emerald Generators upgrade phases
        GamePhase emerald_one = new ResourceGeneratorUpgradePhase(Resource.EMERALD, "II", 60 * 6, 2);
        GamePhase emerald_two = new ResourceGeneratorUpgradePhase(Resource.EMERALD, "III", 60 * 6, 3);

        GamePhase.register(emerald_one);
        GamePhase.register(emerald_two);

        // Game related phases
        GamePhase bed_break = new BedBreakPhase(60 * 10);
        GamePhase sudden_death = new SuddenDeathPhase(60 * 10);

        GamePhase.register(bed_break);
        GamePhase.register(sudden_death);

        XBedwars.default_phases = Collections.unmodifiableList(Arrays.asList(diamond_one, emerald_one, diamond_two, emerald_two, bed_break, sudden_death));
    }

    private void registerUtilities() throws Exception {
        String package_path = "com.slyvr.xbedwars." + Version.getVersionName() + ".utils";

        this.utils = (XBedwarsUtils) Class.forName(package_path + ".BedwarsUtils").getConstructor().newInstance();
        this.utils.register();
    }

    private void loadUsers() {
        UsersManager manager = BedwarsUsersManager.getInstance();

        for (Player player : Bukkit.getOnlinePlayers()) {
            User user = manager.getUser(player);
            if (user != null)
                user.getLevel().setForPlayer(player);
        }

    }

    private void loadDatabase() {
        ConfigurationSection section = getConfig().getConfigurationSection("Database");

        if (!section.getBoolean("Enabled")) {
            logger.info("Database is disabled!");
            return;
        }

        DatabaseType type = DatabaseType.getByName(section.getString("Database-Type"));
        if (type == null) {
            logger.info("Invalid database type! Please check your database info at the config.yml file!");
            return;
        }

        String username = section.getString("Database-Username", "");
        String password = section.getString("Database-Password", "");
        String url = section.getString("Database-URL", "");

        XBedwars.database = getDatabase(type, username, password, url);
        // TODO: Add null check
        if (!database.isConnected()) {
            logger.severe("Could not connect to database! Please check your database info at the config.yml file!");
            return;
        }

        XBedwars.user_data = section.getBoolean("Userdata");
        if (user_data)
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, BedwarsUsersManager::saveAll, 0L, 20 * 60 * 30L);

        XBedwars.logger.info("Successfully connected to database!");
    }

    private void loadPrestiges() {
        PrestigesConfig.getInstance().loadPrestiges();
    }

    private void loadPresets() {
        PresetsConfig.getInstance().loadPresets();
    }

    private void loadLobbies() {
        LobbiesConfig.getInstance().loadLobbies();
    }

    private void loadArenas() {
        BedwarsArena.loadArenas();
    }

    private void loadShops() {
        ItemShopConfig.getInstance().reload();
        UpgradeShopConfig.getInstance().reload();
    }

    @Nullable
    private Database getDatabase(@NotNull DatabaseType type, @NotNull String username, @NotNull String password, @NotNull String url) {
        return new MySQLDatabase(username, password, url);
    }

    public @Nullable Database getDatabase() {
        return database;
    }

    @Override
    public @NotNull UserPrestige getDefaultPrestige() {
        return PrestigesConfig.getInstance().getDefaultPrestige();
    }

    @Override
    public @NotNull UsersManager getUsersManager() {
        return BedwarsUsersManager.getInstance();
    }

    @Override
    public @NotNull LobbiesManager getLobbiesManager() {
        return BedwarsLobbiesManager.getInstance();
    }

    @Override
    public @NotNull ArenasManager getArenasManager() {
        return BedwarsArenasManager.getInstance();
    }

    @Override
    public @NotNull GamesManager getGamesManager() {
        return BedwarsGamesManager.getInstance();
    }

    @Override
    public @NotNull ScoreboardsManager getScoreboardsManager() {
        return BedwarsScoreboardsManager.getInstance();
    }

    @Override
    public @NotNull UpgradesManager getUpgradesManager() {
        return BedwarsUpgradesManager.getInstance();
    }

    @Override
    public @NotNull TrapsManager getTrapsManager() {
        return BedwarsTrapsManager.getInstance();
    }

    @Override
    public @NotNull XBedwarsUtils getUtils() {
        return utils;
    }

    @NotNull
    public static XBedwars getInstance() {
        return XBedwars.instance;
    }

    @NotNull
    public static Logger getBedwarsLogger() {
        return logger;
    }

    @NotNull
    public static List<GamePhase> getDefaultPhases() {
        return default_phases;
    }

    public static boolean isUserDataPersistent() {
        return user_data;
    }

}