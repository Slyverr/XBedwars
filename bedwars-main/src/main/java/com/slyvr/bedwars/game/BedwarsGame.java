package com.slyvr.bedwars.game;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.Titles;
import com.google.common.base.Preconditions;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.bedwars.BedwarsItems;
import com.slyvr.bedwars.api.arena.Arena;
import com.slyvr.bedwars.api.arena.generator.ArenaResourceGeneratorManager;
import com.slyvr.bedwars.api.arena.team.ArenaTeam;
import com.slyvr.bedwars.api.arena.team.ArenaTeamBed;
import com.slyvr.bedwars.api.boss.GameBoss;
import com.slyvr.bedwars.api.boss.GameBossType;
import com.slyvr.bedwars.api.entity.GameEntity;
import com.slyvr.bedwars.api.entity.GameEntityType;
import com.slyvr.bedwars.api.event.EventMessageHandler;
import com.slyvr.bedwars.api.event.game.GameEndEvent;
import com.slyvr.bedwars.api.event.game.GameStartEvent;
import com.slyvr.bedwars.api.event.game.GameStateChangeEvent;
import com.slyvr.bedwars.api.event.player.bed.GamePlayerBedBreakEvent;
import com.slyvr.bedwars.api.event.player.connection.GamePlayerDisconnectEvent;
import com.slyvr.bedwars.api.event.player.connection.GamePlayerReconnectEvent;
import com.slyvr.bedwars.api.event.player.death.GamePlayerDeathByGameBossEvent;
import com.slyvr.bedwars.api.event.player.death.GamePlayerDeathByGameEntityEvent;
import com.slyvr.bedwars.api.event.player.death.GamePlayerDeathByGamePlayerEvent;
import com.slyvr.bedwars.api.event.player.death.GamePlayerDeathEvent;
import com.slyvr.bedwars.api.event.player.elimination.GamePlayerEliminateEvent;
import com.slyvr.bedwars.api.event.player.respawn.GamePlayerRespawnEvent;
import com.slyvr.bedwars.api.event.team.GameTeamEliminateEvent;
import com.slyvr.bedwars.api.event.team.GameTeamShopNPCCreationEvent;
import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.game.GameManager;
import com.slyvr.bedwars.api.game.GameMode;
import com.slyvr.bedwars.api.generator.Resource;
import com.slyvr.bedwars.api.generator.team.TeamResourceGenerator;
import com.slyvr.bedwars.api.generator.team.TeamResourceGeneratorDrop;
import com.slyvr.bedwars.api.generator.team.TeamResourceGeneratorPreset;
import com.slyvr.bedwars.api.generator.tiered.TieredResourceGenerator;
import com.slyvr.bedwars.api.generator.tiered.TieredResourceGeneratorPreset;
import com.slyvr.bedwars.api.lang.Language;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.api.npc.ShopNPC;
import com.slyvr.bedwars.api.npc.ShopNPCType;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.api.player.GamePlayerInventory;
import com.slyvr.bedwars.api.player.GamePlayerRewardManager;
import com.slyvr.bedwars.api.player.GamePlayerStatisticManager;
import com.slyvr.bedwars.api.player.GamePlayerStatisticManager.GamePlayerStatistic;
import com.slyvr.bedwars.api.reward.GameRewardReason;
import com.slyvr.bedwars.api.reward.GameRewardType;
import com.slyvr.bedwars.api.room.WaitingRoom;
import com.slyvr.bedwars.api.room.WaitingRoomTeamManager;
import com.slyvr.bedwars.api.shop.Shop.ShopType;
import com.slyvr.bedwars.api.team.GameTeam;
import com.slyvr.bedwars.api.team.TeamColor;
import com.slyvr.bedwars.api.user.OfflineUser;
import com.slyvr.bedwars.api.user.User;
import com.slyvr.bedwars.api.user.level.UserLevel;
import com.slyvr.bedwars.api.user.level.UserPrestige;
import com.slyvr.bedwars.api.user.stats.UserStatistic;
import com.slyvr.bedwars.api.user.stats.UserStatistics;
import com.slyvr.bedwars.api.user.wallet.UserCurrency;
import com.slyvr.bedwars.api.user.wallet.UserWallet;
import com.slyvr.bedwars.generator.BedwarsTeamResourceGenerator;
import com.slyvr.bedwars.generator.BedwarsTieredResourceGenerator;
import com.slyvr.bedwars.listener.player.GamePlayerBlockListener;
import com.slyvr.bedwars.listener.player.GamePlayerItemListener;
import com.slyvr.bedwars.manager.BedwarsArenasManager;
import com.slyvr.bedwars.manager.BedwarsLobbiesManager;
import com.slyvr.bedwars.manager.BedwarsUsersManager;
import com.slyvr.bedwars.player.BedwarsPlayer;
import com.slyvr.bedwars.room.BedwarsWaitingRoom;
import com.slyvr.bedwars.scoreboard.BedwarsGameScoreboard;
import com.slyvr.bedwars.settings.BedwarsGameSettings;
import com.slyvr.bedwars.team.BedwarsTeam;
import com.slyvr.bedwars.team.BedwarsTeamUtils;
import com.slyvr.bedwars.utils.*;
import com.slyvr.chat.ChatText;
import com.slyvr.chat.ChatTextSection;
import com.slyvr.chat.style.TextAlignment;
import com.slyvr.chat.utils.ChatTextUtils;
import com.slyvr.hologram.VirtualHologram;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;


public final class BedwarsGame implements Game {

    public static final Map<Arena, Game> OCCUPIED_ARENAS_GAMES = new ConcurrentHashMap<>();
    public static final Map<UUID, Game> CONNECTED_PLAYERS_GAMES = new ConcurrentHashMap<>();
    public static final Map<UUID, Game> DISCONNECTED_PLAYERS_GAMES = new ConcurrentHashMap<>();

    private static final Map<Language, ChatTextSection> GAME_DESCRIPTIONS = new HashMap<>();

    static {
        for (Language lang : Language.values()) {
            ChatTextSection desc_section = new ChatTextSection();
            desc_section.append(GameSummary.SEPARATOR);
            desc_section.append(ChatUtils.bold(MessageUtils.formatLangMessage(Message.BEDWARS, lang), ChatColor.YELLOW), TextAlignment.CENTER);
            desc_section.append(GameSummary.EMPTY);

            String description = MessageUtils.formatLangMessage(Message.GAME_START_MESSAGE, lang);
            for (String line : description.split("\\n"))
                desc_section.append(ChatUtils.bold(line, ChatColor.YELLOW), TextAlignment.CENTER);

            desc_section.append(GameSummary.EMPTY);
            desc_section.append(GameSummary.SEPARATOR);

            BedwarsGame.GAME_DESCRIPTIONS.put(lang, desc_section);
        }

    }

    private final Map<Resource, List<TieredResourceGenerator>> resource_generators;
    private final Map<Language, List<VirtualHologram>> virtual_holograms;

    private final Map<TeamColor, GameTeamStorage> available_teams;
    private final Map<TeamColor, GameTeamStorage> eliminated_teams;
    private final Map<UUID, GamePlayerStorage> available_players;

    private final List<ShopNPC> spawned_npcs;

    private final UUID uuid;
    private final Arena arena;
    private final GameMode mode;
    private final GameManager game_manager;
    private final GameWaitingRoomManager waiting_room_manager;
    private final GameSpectatorsManager spectators_manager;
    private final boolean isPrivate;

    private GameState state;
    private GameEnd summary;

    public BedwarsGame(@NotNull Arena arena, boolean prv) {
        Preconditions.checkNotNull(arena, "Game's arena cannot be null!");
        Preconditions.checkState(arena.isReady(), "Game's arena must be ready!");

        if (OCCUPIED_ARENAS_GAMES.containsKey(arena))
            throw new IllegalStateException("Game's arena is already in use by another game!");

        this.uuid = UUID.randomUUID();

        this.arena = arena;
        this.mode = arena.getMode();

        this.game_manager = new BedwarsGameManager(this);
        this.waiting_room_manager = new GameWaitingRoomManager();
        this.spectators_manager = new GameSpectatorsManager();
        this.isPrivate = prv;

        this.available_teams = new EnumMap<>(TeamColor.class);
        this.eliminated_teams = new EnumMap<>(TeamColor.class);

        this.resource_generators = new HashMap<>();
        this.virtual_holograms = new HashMap<>();
        this.available_players = new HashMap<>();

        this.spawned_npcs = new ArrayList<>();

        for (Language language : Language.values())
            virtual_holograms.put(language, new ArrayList<>());

        BedwarsGame.OCCUPIED_ARENAS_GAMES.put(arena, this);
        this.callStateChange(GameState.WAITING);
    }

    public BedwarsGame(@NotNull Arena arena) {
        this(arena, false);
    }

    @NotNull
    public static Collection<Game> getGames() {
        return Collections.unmodifiableCollection(OCCUPIED_ARENAS_GAMES.values());
    }

    @Nullable
    public static Game getArenaGame(@NotNull Arena arena) {
        return OCCUPIED_ARENAS_GAMES.get(arena);
    }

    @Nullable
    public static Game getPlayerGame(@NotNull Player player) {
        return player != null ? CONNECTED_PLAYERS_GAMES.get(player.getUniqueId()) : null;
    }

    @Nullable
    public static Game getDisconnectedPlayerGame(@NotNull Player player) {
        return player != null ? DISCONNECTED_PLAYERS_GAMES.get(player.getUniqueId()) : null;
    }

    @Nullable
    public static Game removeFromGame(@NotNull Player player) {
        if (player == null)
            return null;

        Game game = CONNECTED_PLAYERS_GAMES.get(player.getUniqueId());
        return game != null && (game.getWaitingRoom().removePlayer(player) || game.disconnect(player)) ? game : null;
    }

    @Nullable
    public static Game addToRandomGame(@NotNull Player player, @Nullable Predicate<Game> predicate) {
        if (player == null)
            return null;

        for (Game game : OCCUPIED_ARENAS_GAMES.values()) {
            if (game.isPrivate())
                continue;

            if (predicate != null && !predicate.test(game))
                continue;

            if (game.getWaitingRoom().addPlayer(player))
                return game;
        }

        return null;
    }

    @Nullable
    public static Game addToRandomArena(@NotNull Player player, @Nullable Predicate<Arena> predicate) {
        Arena ready_arena = BedwarsArenasManager.getInstance().getRandomArena(predicate);
        if (ready_arena == null)
            return null;

        if (OCCUPIED_ARENAS_GAMES.containsKey(ready_arena))
            return null;

        if (!ready_arena.isReady())
            return null;

        Game game = new BedwarsGame(ready_arena);
        return game.getWaitingRoom().addPlayer(player) ? game : null;
    }

    @Nullable
    public static Game randomGame(@Nullable Predicate<Game> predicate) {
        List<Game> games = new ArrayList<>(OCCUPIED_ARENAS_GAMES.size());

        for (Game game : OCCUPIED_ARENAS_GAMES.values()) {
            if (predicate == null || predicate.test(game))
                games.add(game);
        }

        return !games.isEmpty() ? games.get(ThreadLocalRandom.current().nextInt(games.size())) : null;
    }

    public static int getPlayersCount(@NotNull GameMode mode) {
        int result = 0;

        for (Game game : OCCUPIED_ARENAS_GAMES.values()) {
            if (game.getMode().equals(mode))
                result += game.size();
        }

        return result;
    }

    public static boolean inGame(@NotNull Player player) {
        return player != null && CONNECTED_PLAYERS_GAMES.containsKey(player.getUniqueId());
    }

    public static boolean inRunningGame(@NotNull Player player) {
        if (player == null)
            return false;

        Game game = CONNECTED_PLAYERS_GAMES.get(player.getUniqueId());
        return game != null && game.isRunning();
    }

    public static boolean isOccupied(@NotNull Arena arena) {
        return arena != null && OCCUPIED_ARENAS_GAMES.containsKey(arena);
    }

    public static void stopAll(boolean force) {
        for (Game game : OCCUPIED_ARENAS_GAMES.values())
            game.stop(force);
    }

    public static void stopAll() {
        BedwarsGame.stopAll(false);
    }

    @NotNull
    private static String getColoredPlayerName(@NotNull GamePlayer player) {
        return player.getTeamColor().getChatColor() + player.getPlayer().getDisplayName();
    }

    @Override
    public @NotNull Arena getArena() {
        return arena;
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return uuid;
    }

    @Override
    public boolean start() {
        if (state == GameState.ENDED || !waiting_room_manager.getWaitingRoom().isReady())
            return false;

        GameStartEvent event = new GameStartEvent(this);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return false;

        this.callStateChange(GameState.STARTING);

        // Gets the drops for teams resource-generator.
        ArenaResourceGeneratorManager generator_manager = arena.getResourceGeneratorManager();

        TeamResourceGeneratorPreset teams_preset = generator_manager.getTeamResourceGeneratorPreset();
        Set<TeamResourceGeneratorDrop> drops = teams_preset.getDrops();

        // Spawns each full team and assigns a team to players without.
        Collection<ArenaTeam> empty_teams = new ArrayList<>(8);
        Collection<ArenaTeam> ready_teams = arena.getReadyTeams();

        WaitingRoomTeamManager team_manager = waiting_room_manager.getWaitingRoom().getTeamManager();
        Iterator<Player> unassigned_players = team_manager.getUnassignedPlayers().iterator();

        int team_max = mode.getTeamMax();
        for (ArenaTeam team : ready_teams) {
            Set<Player> team_players = team_manager.getAssignedPlayers(team.getColor());

            int required = team_max - team_players.size();
            while (required > 0 && unassigned_players.hasNext()) {
                Player unassigned = unassigned_players.next();
                unassigned_players.remove();

                team_players.add(unassigned);
                if ((--required) == 0)
                    break;
            }

            this.createTeam(team, team_players, drops);
            if (team_players.isEmpty())
                empty_teams.add(team);

            Chest chest = team.getChest();
            if (chest != null)
                chest.getInventory().clear();
        }

        Bukkit.getScheduler().runTaskLater(Bedwars.getInstance(), () -> {
            for (ArenaTeam team : empty_teams) {
                ArenaTeamBed bed = team.getBed();
                if (bed != null)
                    bed.destroy();

                this.available_teams.remove(team.getColor());
            }

            if (available_teams.size() == 1)
                this.stop();

        }, 120 * 20L);

        for (Resource resource : Resource.values()) {
            List<Location> locations = generator_manager.getResourceGeneratorLocations(resource);
            if (locations.isEmpty())
                continue;

            TieredResourceGeneratorPreset preset = generator_manager.getTieredResourceGeneratorPreset(resource);
            if (preset == null)
                continue;

            List<TieredResourceGenerator> generators = new ArrayList<>();
            for (Location location : locations) {
                TieredResourceGenerator generator = new BedwarsTieredResourceGenerator(this, location, resource, preset);
                generator.start();

                generators.add(generator);
            }

            this.resource_generators.put(resource, generators);
        }

        for (GamePlayerStorage storage : available_players.values()) {
            Language language = BedwarsUsersManager.getPlayerLanguage(storage.player.getPlayer());

            List<VirtualHologram> holograms = virtual_holograms.get(language);
            if (holograms == null)
                continue;

            for (VirtualHologram hologram : holograms) {
                hologram.addViewer(storage.player.getPlayer());
                hologram.update();
            }

        }

        this.waiting_room_manager.getWaitingRoom().clear();
        this.waiting_room_manager.destroy();
        this.game_manager.start();

        this.callStateChange(GameState.RUNNING);
        return true;
    }

    @Override
    public boolean stop() {
        return stop(!Bedwars.getInstance().isEnabled());
    }

    @Override
    public boolean stop(boolean force) {
        if (state != GameState.RUNNING) {
            if (force) {
                if (state != GameState.ENDING) {
                    this.getWaitingRoom().clear();
                    this.reset();
                    return true;
                }

                this.available_players.values().forEach(storage -> summary.remove(storage, true));
                this.summary.clear();
                this.resetArena();
                return true;
            }

            return false;
        }

        this.callStateChange(GameState.ENDING);

        this.summary = new GameEnd();
        summary.stop(force);

        for (ShopNPC npc : spawned_npcs)
            npc.remove();

        for (GameTeamStorage data : available_teams.values())
            data.remove();

        for (GameTeamStorage data : eliminated_teams.values())
            data.remove();

        for (List<TieredResourceGenerator> generators : resource_generators.values()) {
            for (TieredResourceGenerator generator : generators)
                generator.stop();
        }

        for (List<VirtualHologram> holograms : virtual_holograms.values()) {
            for (VirtualHologram hologram : holograms)
                hologram.remove();
        }

        this.resource_generators.clear();
        this.virtual_holograms.clear();

        this.available_teams.clear();
        this.eliminated_teams.clear();
        this.spawned_npcs.clear();

        this.game_manager.stop();

        if (!force)
            Bukkit.getScheduler().runTaskLater(Bedwars.getInstance(), this::resetArena, 60 * 20L);
        else
            this.resetArena();

        return true;
    }

    private void createTeam(@NotNull ArenaTeam team, @NotNull Set<Player> players, @NotNull Set<TeamResourceGeneratorDrop> drops) {
        TeamResourceGenerator generator = new BedwarsTeamResourceGenerator(this, team.getResourceGeneratorLocation(), team.getColor(), drops);
        generator.start();

        GameTeam game_team = new BedwarsTeam(this, team.getColor(), generator);
        GameTeamStorage team_storage = new GameTeamStorage(game_team, new HashSet<>(players.size(), 1F), team.getSpawnLocation());

        this.spawnTeamShop(game_team, ShopType.ITEMS, team.getShopNPCLocation(ShopType.ITEMS));
        this.spawnTeamShop(game_team, ShopType.UPGRADES, team.getShopNPCLocation(ShopType.UPGRADES));

        if (!players.isEmpty())
            this.spawnTeamPlayers(team_storage, players);

        this.available_teams.put(team.getColor(), team_storage);
    }

    private void spawnTeamPlayers(@NotNull GameTeamStorage storage, @NotNull Collection<Player> players) {
        for (Player player : players)
            this.spawnTeamPlayer(storage, player);
    }

    private void spawnTeamPlayer(@NotNull GameTeamStorage team_storage, @NotNull Player player) {
        TeamColor color = team_storage.getGameTeam().getColor();
        Location spawn = team_storage.getTeamSpawn();

        PlayerUtils.setPlayerListName(player, color.getColoredRepresentingChar() + ' ' + player.getDisplayName());
        PlayerUtils.setPlayerGameMode(player, org.bukkit.GameMode.SURVIVAL);

        PlayerUtils.resetPlayerHealth(player);
        PlayerUtils.resetPlayerFood(player);
        PlayerUtils.clear(player);

        player.getInventory().setItem(0, BedwarsItems.SWORD);
        player.getEnderChest().clear();

        player.setCanPickupItems(true);
        player.teleport(spawn);

        ChatTextSection description = GAME_DESCRIPTIONS.get(MessageUtils.getPlayerLanguage(player));
        if (description != null)
            description.sendSection(player);

        this.available_players.put(player.getUniqueId(), new GamePlayerStorage(team_storage, new BedwarsPlayer(this, player, color)));
    }

    private void spawnTeamShop(@NotNull GameTeam team, @NotNull ShopType type, @NotNull Location loc) {
        GameTeamShopNPCCreationEvent event = new GameTeamShopNPCCreationEvent(team, BedwarsGameSettings.VILLAGER, type, loc);
        Bukkit.getPluginManager().callEvent(event);

        ShopNPC npc = ShopNPCType.create(event.getNPCType(), team, type, loc);
        npc.spawn();

        Message message = type != ShopType.ITEMS ? Message.SHOP_NPC_TITLE_UPGRADES : Message.SHOP_NPC_TITLE_ITEMS;
        for (Entry<Language, List<VirtualHologram>> entry : virtual_holograms.entrySet()) {
            VirtualHologram hologram = new VirtualHologram(loc.add(0, 1, 0));
            hologram.addText(Message.INTERACTION_RIGHT_CLICK.format(entry.getKey()));
            hologram.addText(message.format(entry.getKey()));

            entry.getValue().add(hologram);
        }

        spawned_npcs.add(npc);
    }

    private void resetArena() {
        this.callStateChange(GameState.RESETTING);

        GamePlayerBlockListener.reset(this);
        GamePlayerItemListener.reset(this);

        this.waiting_room_manager.construct();
        for (ArenaTeam team : arena.getReadyTeams()) {
            ArenaTeamBed bed = team.getBed();
            if (bed != null)
                bed.place();

            Chest chest = team.getChest();
            if (chest != null)
                chest.getBlockInventory().clear();
        }

        this.reset();
    }

    private void reset() {
        OCCUPIED_ARENAS_GAMES.remove(arena);
        this.callStateChange(GameState.ENDED);
    }

    @Override
    public @NotNull WaitingRoom getWaitingRoom() {
        return waiting_room_manager.getWaitingRoom();
    }

    @Override
    public @NotNull GameManager getManager() {
        return game_manager;
    }

    @Override
    public @NotNull GameMode getMode() {
        return mode;
    }

    @Override
    public @NotNull GameState getState() {
        return state;
    }

    @Override
    public void setState(@NotNull GameState state) {
        if (state == null || this.state == state)
            return;

        switch (state) {
            case WAITING:
                if (this.state == GameState.COUNTDOWN || this.state == GameState.RESETTING)
                    this.callStateChange(state);

                return;
            case COUNTDOWN:
                if (this.state == GameState.WAITING)
                    this.callStateChange(state);

                return;
            case RUNNING:
                this.start();
                return;
            case ENDED:
                this.stop();
        }

    }

    private void callStateChange(@NotNull GameState state) {
        GameStateChangeEvent event = new GameStateChangeEvent(this, this.state, this.state = state);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public @NotNull Collection<GameTeam> getGameTeams() {
        Set<GameTeam> result = new HashSet<>(available_teams.size() + eliminated_teams.size());

        available_teams.values().forEach(storage -> result.add(storage.team));
        eliminated_teams.values().forEach(storage -> result.add(storage.team));

        return result;
    }

    @Override
    public @Nullable GameTeam getGameTeam(@NotNull TeamColor color) {
        if (color == null)
            return null;

        GameTeamStorage storage = available_teams.getOrDefault(color, eliminated_teams.get(color));
        return storage != null ? storage.getGameTeam() : null;
    }

    @Override
    public @NotNull Collection<GamePlayer> getTeamPlayers(@NotNull TeamColor color) {
        if (color == null)
            return new HashSet<>(0);

        GameTeamStorage storage = available_teams.getOrDefault(color, eliminated_teams.get(color));
        return storage != null ? storage.getTeamPlayers() : new HashSet<>(0);
    }

    @Override
    public @NotNull Collection<GamePlayer> getGamePlayers() {
        Set<GamePlayer> result = new HashSet<>(available_players.size());

        for (GamePlayerStorage storage : available_players.values())
            result.add(storage.player);

        return result;
    }

    @Override
    public @Nullable GamePlayer getGamePlayer(@NotNull Player player) {
        if (player == null)
            return null;

        GamePlayerStorage storage = available_players.get(player.getUniqueId());
        return storage != null ? storage.getGamePlayer() : null;
    }

    @Override
    public @NotNull Collection<TieredResourceGenerator> getResourceGenerators(@NotNull Resource resource) {
        if (resource == null)
            return new HashSet<>(0);

        List<TieredResourceGenerator> generators = resource_generators.get(resource);
        return generators != null ? new ArrayList<>(generators) : new ArrayList<>(0);
    }

    @Override
    public boolean killPlayer(@NotNull Player player, @Nullable Entity killer, @Nullable DamageCause cause, int respawn) {
        GamePlayer dead = getGamePlayer(player);
        return dead != null && killPlayer(dead, killer, cause, respawn, !hasBed(dead.getTeamColor()));
    }

    @Override
    public boolean killPlayer(@NotNull Player player, @Nullable Entity killer, @Nullable DamageCause cause) {
        return killPlayer(player, killer, cause, BedwarsGameSettings.getDefaultRespawnTime());
    }

    @Override
    public boolean killPlayer(@NotNull Player player, @Nullable Entity killer) {
        return killPlayer(player, killer, DamageCause.CUSTOM);
    }

    @Override
    public boolean killPlayer(@NotNull Player player) {
        return killPlayer(player, null);
    }

    private boolean killPlayer(@NotNull GamePlayer player, @Nullable Entity killer, @Nullable DamageCause cause, int respawn, boolean isFinal) {
        if (!killPlayer(player, killer, cause, isFinal))
            return false;

        if (!isFinal) {
            this.spectators_manager.setSpectator(player.getPlayer(), respawn);
            return true;
        }

        GamePlayerEliminateEvent eliminate_event = new GamePlayerEliminateEvent(player);
        Bukkit.getPluginManager().callEvent(eliminate_event);

        this.spectators_manager.setSpectator(player.getPlayer());
        MessageUtils.sendLangMessage(Message.PLAYER_ELIMINATION, player.getPlayer());

        this.checkTeamElimination(player.getTeamColor());
        return true;
    }

    private boolean killPlayer(@NotNull GamePlayer dead, @Nullable Entity killer, @Nullable DamageCause cause, boolean isFinal) {
        if (cause == null)
            cause = DamageCause.CUSTOM;

        Map<Resource, Integer> drops = getDrops(dead);
        if (killer == null) {
            this.killPlayer(dead, drops, cause, isFinal);
            return true;
        }

        if (killer instanceof Player) {
            GamePlayer killer_player = getGamePlayer((Player) killer);
            if (killer_player == null) {
                this.killPlayer(dead, drops, cause, isFinal);
                return true;
            }

            if (killer_player.getTeamColor() == dead.getTeamColor())
                return false;

            GamePlayerDeathByGamePlayerEvent event = new GamePlayerDeathByGamePlayerEvent(dead, killer_player, drops, cause, isFinal);
            Bukkit.getPluginManager().callEvent(event);

            this.broadcastEventMessage(event.getDeathMessageHandler(), getColoredPlayerName(dead), getColoredPlayerName(killer_player));
            this.checkDrops(event, killer_player);

            this.incrementStatistic(killer_player, isFinal ? GamePlayerStatistic.FINAL_KILLS : GamePlayerStatistic.KILLS, 1);
            this.incrementStatistic(dead, isFinal ? GamePlayerStatistic.FINAL_DEATHS : GamePlayerStatistic.DEATHS, 1);

            this.incrementReward(killer_player, isFinal ? GameRewardReason.FINAL_KILL : GameRewardReason.KILL);
            return true;
        }

        GameEntity game_entity = GameEntityType.getGameEntity(killer);
        if (game_entity != null) {
            GamePlayer killer_player = game_entity.getOwner();
            if (killer_player.getTeamColor() == dead.getTeamColor())
                return false;

            GamePlayerDeathByGameEntityEvent event = new GamePlayerDeathByGameEntityEvent(dead, game_entity, drops, cause, isFinal);
            Bukkit.getPluginManager().callEvent(event);

            this.broadcastEventMessage(event.getDeathMessageHandler(), getColoredPlayerName(dead), getColoredPlayerName(killer_player));
            this.checkDrops(event, killer_player);

            this.incrementStatistic(killer_player, isFinal ? GamePlayerStatistic.FINAL_KILLS : GamePlayerStatistic.KILLS, 1);
            this.incrementStatistic(dead, isFinal ? GamePlayerStatistic.FINAL_DEATHS : GamePlayerStatistic.DEATHS, 1);

            this.incrementReward(killer_player, isFinal ? GameRewardReason.FINAL_KILL : GameRewardReason.KILL);
            return true;
        }

        GameBoss game_boss = GameBossType.getGameBoss(killer);
        if (game_boss != null) {
            if (game_boss.getGameTeam().getColor() == dead.getTeamColor())
                return false;

            GamePlayerDeathByGameBossEvent event = new GamePlayerDeathByGameBossEvent(dead, game_boss, drops, cause, isFinal);
            Bukkit.getPluginManager().callEvent(event);

            this.broadcastEventMessage(event.getDeathMessageHandler(), getColoredPlayerName(dead), game_boss.getGameTeam().getColor().getColoredName());
            this.incrementStatistic(dead, isFinal ? GamePlayerStatistic.FINAL_DEATHS : GamePlayerStatistic.DEATHS, 1);
            return true;
        }

        this.killPlayer(dead, drops, cause, isFinal);
        return true;
    }

    private void killPlayer(@NotNull GamePlayer dead, @NotNull Map<Resource, Integer> drops, @NotNull DamageCause cause, boolean isFinal) {
        GamePlayerDeathEvent event = new GamePlayerDeathEvent(dead, drops, cause, isFinal);
        Bukkit.getPluginManager().callEvent(event);

        this.broadcastEventMessage(event.getDeathMessageHandler(), getColoredPlayerName(dead));
        this.incrementStatistic(dead, isFinal ? GamePlayerStatistic.FINAL_DEATHS : GamePlayerStatistic.DEATHS, 1);
    }

    private void checkDrops(@NotNull GamePlayerDeathEvent event, @Nullable GamePlayer killer) {
        GamePlayer dead = event.getGamePlayer();

        if (killer != null && !spectators_manager.isSpectator(killer.getPlayer())) {
            Map<Resource, Integer> drops = event.getDrops();
            Player player = killer.getPlayer();

            ItemStack[] drops_items = new ItemStack[drops.size()];
            String[] drops_texts = new String[drops.size()];

            int index = 0;
            for (Entry<Resource, Integer> entry : drops.entrySet()) {
                Resource rsc = entry.getKey();
                if (rsc == null)
                    continue;

                int amount = entry.getValue();
                if (amount <= 0)
                    continue;

                drops_items[index] = new ItemStack(rsc.getMaterial(), amount);
                drops_texts[index++] = String.valueOf(rsc.getColor()) + '+' + amount + ' ' + rsc.getName();
            }

            Map<Integer, ItemStack> not_added = player.getInventory().addItem(drops_items);

            Location player_loc = player.getLocation();
            for (ItemStack item : not_added.values())
                player_loc.getWorld().dropItem(player_loc, item);

            for (String drop_text : drops_texts)
                player.sendMessage(drop_text);
        }

        if (!event.isFinalKill())
            return;

        GameTeamStorage storage = available_teams.get(dead.getTeamColor());
        if (storage == null)
            return;

        Location generator_loc = storage.getGameTeam().getResourceGenerator().getDropLocation();
        for (ItemStack item : dead.getPlayer().getEnderChest()) {
            if (item != null)
                generator_loc.getWorld().dropItemNaturally(generator_loc, item);
        }

        dead.getPlayer().getEnderChest().clear();
        MessageUtils.sendLangMessage(Message.PLAYER_CONTENT_DROPPED, killer.getPlayer(), dead.getPlayer().getDisplayName());
    }

    @Override
    public boolean eliminatePlayer(@NotNull Player player) {
        GamePlayer game_player = getGamePlayer(player);
        return game_player != null && killPlayer(game_player, null, null, -1, true);
    }

    @Override
    public boolean isEliminated(@NotNull Player player) {
        if (player == null)
            return false;

        GamePlayerStorage data = available_players.get(player.getUniqueId());
        return data != null && data.isEliminated;
    }

    @Override
    public boolean eliminateTeam(@NotNull TeamColor color) {
        GameTeamStorage storage = available_teams.get(color);
        if (storage == null)
            return false;

        this.eliminateTeam(storage);
        return true;
    }

    @Override
    public boolean isEliminated(@NotNull TeamColor color) {
        return !available_teams.containsKey(color);
    }

    private void checkTeamElimination(@NotNull TeamColor color) {
        GameTeamStorage storage = available_teams.get(color);
        if (storage == null)
            return;

        for (GamePlayerStorage player_storage : storage.players) {
            if (!player_storage.isEliminated)
                return;
        }

        this.eliminateTeam(storage);
    }

    private void eliminateTeam(@NotNull GameTeamStorage storage) {
        GameTeam team = storage.getGameTeam();
        TeamColor color = team.getColor();

        GameTeamEliminateEvent event = new GameTeamEliminateEvent(team);
        Bukkit.getPluginManager().callEvent(event);

        for (GamePlayerStorage player_storage : storage.players) {
            if (!player_storage.isEliminated)
                this.killPlayer(player_storage.getGamePlayer(), null, null, -1, true);
        }

        for (GamePlayerStorage player_storage : available_players.values()) {
            if (player_storage.isDisconnected)
                continue;

            Player player = player_storage.getGamePlayer().getPlayer();

            player.sendMessage("");
            player.sendMessage(MessageUtils.formatEventMessage(event.getEliminationMessageHandler(), player, color.getColoredName()));
            player.sendMessage("");
        }

        this.eliminated_teams.put(color, storage);
        this.available_teams.remove(color);

        if (available_teams.size() == 1)
            this.stop();
    }

    @Override
    public boolean reconnect(@NotNull Player player) {
        if (player == null || !DISCONNECTED_PLAYERS_GAMES.remove(player.getUniqueId(), this))
            return false;

        GamePlayerStorage player_storage = available_players.get(player.getUniqueId());
        if (player_storage == null)
            return false;

        // Refreshing the player
        BedwarsPlayer bedwars_player = (BedwarsPlayer) player_storage.getGamePlayer();
        bedwars_player.refresh();

        GamePlayerReconnectEvent event = new GamePlayerReconnectEvent(bedwars_player);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return false;

        // Reset player's game connection
        BedwarsGame.CONNECTED_PLAYERS_GAMES.put(player.getUniqueId(), this);
        player_storage.isDisconnected = false;

        // Displaying holograms
        for (VirtualHologram hologram : virtual_holograms.get(MessageUtils.getPlayerLanguage(player))) {
            hologram.addViewer(player);
            hologram.update();
        }

        PlayerUtils.setPlayerTime(player, arena.getTime(), true);
        if (!hasBed(bedwars_player.getTeamColor())) {
            this.spectators_manager.setSpectator(player);
            return true;
        }

        this.spectators_manager.setSpectator(player, BedwarsGameSettings.getReconnectRespawnTime());
        this.broadcastEventMessage(event.getReconnectMessageHandler(), getColoredPlayerName(bedwars_player));
        return true;
    }

    @Override
    public boolean disconnect(@NotNull Player player) {
        GamePlayer game_player = getGamePlayer(player);
        if (game_player == null)
            return false;

        BedwarsGame.CONNECTED_PLAYERS_GAMES.remove(player.getUniqueId());
        BedwarsGame.DISCONNECTED_PLAYERS_GAMES.put(player.getUniqueId(), this);

        PlayerUtils.resetPlayerScoreboard(player);
        PlayerUtils.resetPlayerFlight(player);
        PlayerUtils.resetPlayerHealth(player);
        PlayerUtils.clear(player);

        GamePlayerStorage storage = available_players.get(player.getUniqueId());
        storage.isDisconnected = true;

        BedwarsLobbiesManager.getInstance().sendToRandomLobby(player);

        GamePlayerDisconnectEvent event = new GamePlayerDisconnectEvent(game_player);
        Bukkit.getPluginManager().callEvent(event);

        this.broadcastEventMessage(event.getDisconnectMessageHandler(), getColoredPlayerName(game_player));

        TeamColor color = game_player.getTeamColor();
        if (!hasBed(color))
            return killPlayer(game_player, null, null, -1, true);

        Bukkit.getScheduler().runTaskLater(Bedwars.getInstance(), () -> {
            GameTeamStorage team_storage = storage.getTeamStorage();

            for (GamePlayerStorage player_storage : team_storage.players) {
                if (!player_storage.isDisconnected)
                    return;
            }

            BedwarsGame.this.breakTeamBed(color);
        }, 120 * 20L);
        return true;
    }

    @Override
    public boolean isDisconnected(@NotNull Player player) {
        if (player == null)
            return false;

        GamePlayerStorage storage = available_players.get(player.getUniqueId());
        return storage != null && storage.isDisconnected;
    }

    @Override
    public @NotNull Collection<Player> getSpectators() {
        return spectators_manager.getAddedSpectators();
    }

    @Override
    public boolean addSpectator(@NotNull Player player) {
        return spectators_manager.addSpectator(player);
    }

    @Override
    public boolean isSpectator(@NotNull Player player) {
        return spectators_manager.isSpectator(player);
    }

    @Override
    public boolean breakTeamBed(@NotNull TeamColor color, @Nullable Player destroyer) {
        if (destroyer == null)
            return breakTeamBed(color);

        if (color == null)
            return false;

        ArenaTeamBed bed = arena.getTeam(color).getBed();
        if (bed == null)
            return false;

        GamePlayerStorage storage = available_players.get(destroyer.getUniqueId());
        if (storage == null)
            return false;

        GamePlayer game_player = storage.getGamePlayer();
        if (bed.getTeamColor() == game_player.getTeamColor()) {
            game_player.sendMessage(Message.BED_BREAK_OWNED);
            return false;
        }

        GamePlayerBedBreakEvent event = new GamePlayerBedBreakEvent(game_player, bed);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled() || !bed.destroy())
            return false;

        for (GamePlayerStorage other_storage : available_players.values()) {
            GamePlayer storage_player = other_storage.getGamePlayer();
            if (storage_player.getTeamColor() != bed.getTeamColor()) {
                this.sendBedBreakMessage(storage_player, event);
                continue;
            }

            this.incrementStatistic(storage_player, GamePlayerStatistic.BEDS_LOST, 1);
            if (other_storage.isDisconnected) {
                other_storage.isEliminated = true;
                continue;
            }

            this.sendBedLostMessage(storage_player, event);
        }

        this.incrementStatistic(game_player, GamePlayerStatistic.BEDS_BROKEN, 1);
        this.incrementReward(game_player, GameRewardReason.BED_DESTROYED);

        this.checkTeamElimination(bed.getTeamColor());
        return true;
    }

    @Override
    public boolean breakTeamBed(@NotNull TeamColor color) {
        GameTeamStorage team_storage = available_teams.get(color);
        if (team_storage == null)
            return false;

        ArenaTeamBed bed = arena.getTeam(color).getBed();
        if (bed == null || !bed.destroy())
            return false;

        for (GamePlayerStorage storage : team_storage.players) {
            GamePlayer storage_player = storage.getGamePlayer();

            if (storage.isDisconnected) {
                storage.isEliminated = true;
                continue;
            }

            this.incrementStatistic(storage_player, GamePlayerStatistic.BEDS_LOST, 1);
            this.sendBedLostMessage(storage_player, null);
        }

        this.checkTeamElimination(bed.getTeamColor());
        return true;
    }

    @Override
    public boolean hasBed(@NotNull TeamColor color) {
        if (color == null)
            return false;

        ArenaTeam arena_team = arena.getTeam(color);
        if (arena_team == null)
            return false;

        ArenaTeamBed bed = arena_team.getBed();
        return bed != null && !bed.isDestroyed();
    }

    private void sendBedBreakMessage(@NotNull GamePlayer player, @NotNull GamePlayerBedBreakEvent event) {
        Player bukkit_player = player.getPlayer();

        MessageUtils.sendEventMessage(event.getBedBreakMessageHandler(), bukkit_player, event.getBed().getTeamColor().getColoredName(), getColoredPlayerName(event.getGamePlayer()));
        XSound.ENTITY_ENDER_DRAGON_GROWL.play(bukkit_player);
    }

    // Helper methods

    private void sendBedLostMessage(@NotNull GamePlayer player, @Nullable GamePlayerBedBreakEvent event) {
        Player bukkit_player = player.getPlayer();
        if (event != null)
            MessageUtils.sendEventMessage(event.getBedLostMessageHandler(), bukkit_player, getColoredPlayerName(event.getGamePlayer()));

        String title = MessageUtils.formatLangMessage(Message.BED_LOST_TITLE, bukkit_player);
        String sub = MessageUtils.formatLangMessage(Message.BED_LOST_SUBTITLE, bukkit_player);

        Titles.sendTitle(bukkit_player, 15, 30, 15, title, sub);
        XSound.ENTITY_WITHER_DEATH.play(player.getPlayer());
    }

    private void incrementStatistic(@NotNull GamePlayer player, @NotNull GamePlayerStatistic stat, int amount) {
        GamePlayerStatisticManager manager = player.getStatisticManager();
        manager.incrementStatistic(stat, amount);
    }

    private void incrementReward(@NotNull GamePlayer player, @NotNull GameRewardReason reason) {
        GamePlayerStorage storage = available_players.get(player.getPlayer().getUniqueId());
        GamePlayerRewardManager manager = player.getRewardManager();

        Language lang = MessageUtils.getPlayerLanguage(player.getPlayer());
        for (GameRewardType type : GameRewardType.values()) {
            int amount = BedwarsGameSettings.getRewardAmount(type, reason, mode);
            if (amount <= 0)
                continue;

            manager.increment(type, reason, amount);
            if (!storage.isDisconnected)
                player.sendMessage(type.getColor() + "+" + amount + ' ' + type.getName(lang) + "! (" + reason.getReason(lang) + ")");
        }
    }

    private void broadcastEventMessage(@NotNull EventMessageHandler handler, Object... args) {
        for (GamePlayerStorage storage : available_players.values()) {
            if (storage.isDisconnected)
                continue;

            Player player = storage.getGamePlayer().getPlayer();
            player.sendMessage(handler.format(MessageUtils.getPlayerLanguage(player), args));
        }

    }

    @Override
    public void broadcastMessage(@NotNull String message, @Nullable Predicate<Player> predicate) {
        if (message == null)
            return;

        for (GamePlayerStorage storage : available_players.values()) {
            if (storage.isDisconnected)
                continue;

            Player player = storage.getGamePlayer().getPlayer();
            if (predicate == null || predicate.test(player))
                player.sendMessage(message);
        }
    }

    @Override
    public void broadcastMessage(@NotNull String message) {
        this.broadcastMessage(message, null);
    }

    @Override
    public void forEach(@NotNull Consumer<GamePlayer> action) {
        if (action == null)
            return;

        for (GamePlayerStorage storage : available_players.values())
            action.accept(storage.getGamePlayer());
    }

    @Override
    public boolean contains(@NotNull Player player) {
        return player != null && available_players.containsKey(player.getUniqueId());
    }

    @Override
    public boolean isPrivate() {
        return isPrivate;
    }

    @Override
    public boolean isRunning() {
        return state == GameState.RUNNING;
    }

    @Override
    public int size() {
        return isRunning() ? available_players.size() : waiting_room_manager.room.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof BedwarsGame))
            return false;

        BedwarsGame other = (BedwarsGame) obj;
        return uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        return "BedwarsGame{" +
                "uuid=" + uuid +
                ", arena=" + arena +
                ", mode=" + mode +
                ", state=" + state +
                ", private=" + isPrivate +
                '}';
    }

    @NotNull
    private Map<Resource, Integer> getDrops(@NotNull GamePlayer dead) {
        Map<Resource, Integer> result = new HashMap<>(10);

        for (ItemStack item : dead.getPlayer().getInventory()) {
            if (item == null)
                continue;

            Resource rsc = Resource.getByMaterial(item.getType());
            if (rsc == null)
                continue;

            Integer old = result.get(rsc);
            result.put(rsc, old != null ? old + item.getAmount() : item.getAmount());
        }

        return result;
    }

    private final static class GameSummary {

        private static final Comparator<Entry<Player, Integer>> COMPARATOR = (e1, e2) -> e2.getValue().compareTo(e1.getValue());

        private static final char RECTANGLE = '▬';
        private static final char SQUARE = '■';
        private static final char CIRCLE = '・';

        private static final String PROGRESS_BAR;
        private static final String SEPARATOR;
        private static final String EMPTY;

        static {
            ChatTextUtils.setCharacterWidth(RECTANGLE, 3, 4);
            ChatTextUtils.setCharacterWidth(SQUARE, 6, 6);
            ChatTextUtils.setCharacterWidth(CIRCLE, 1, 2);

            StringBuilder builder = new StringBuilder(68);
            builder.append(ChatColor.GREEN);
            builder.append(ChatColor.BOLD);

            for (int i = 0; i < 64; i++)
                builder.append(RECTANGLE);

            PROGRESS_BAR = UserLevel.getProgressBar(34, 1F);
            SEPARATOR = builder.toString();
            EMPTY = "";
        }

        private final Map<Player, Integer> top_players;
        private final Set<Player> winners;

        private final TeamColor winner;

        public GameSummary(@NotNull Map<Player, Integer> players, @NotNull Set<Player> winners, @Nullable TeamColor winner) {
            this.top_players = players;
            this.winners = winners;

            this.winner = winner;
        }

        public void sendSummary(@NotNull GamePlayer player) {
            if (player == null)
                return;

            this.sendRewardSummary(player);
            this.sendPlayersSummary(player);
        }

        private void sendRewardSummary(@NotNull GamePlayer player) {
            GamePlayerRewardManager manager = player.getRewardManager();
            Player bukkit_player = player.getPlayer();

            ChatTextSection summary = new ChatTextSection();
            summary.append(SEPARATOR);
            summary.append(MessageUtils.formatLangMessage(Message.GAME_SUMMARY_REWARDS, bukkit_player), TextAlignment.CENTER);
            summary.append(EMPTY);
            summary.append(MessageUtils.formatLangMessage(Message.GAME_SUMMARY_REWARDS_EARNINGS_LIST, bukkit_player), 2);

            Language lang = MessageUtils.getPlayerLanguage(bukkit_player);
            summary.append(getRewardText(manager, GameRewardType.BEDWARS_COINS, lang), 4);

            for (GameRewardType type : manager.getRewards()) {
                if (type == GameRewardType.BEDWARS_EXPERIENCE || type == GameRewardType.BEDWARS_COINS)
                    continue;

                if (manager.getTotalAmount(type) == 0)
                    continue;

                summary.append(getRewardText(manager, type, lang));
            }

            summary.append(EMPTY);
            this.appendExperienceSummary(summary, manager, bukkit_player);
            summary.append(EMPTY);

            summary.append(format(GameRewardType.BEDWARS_EXPERIENCE, manager, bukkit_player));

            summary.append(EMPTY);
            summary.append(SEPARATOR);

            summary.sendSection(bukkit_player);
        }

        private void sendPlayersSummary(@NotNull GamePlayer player) {
            Player bukkit_player = player.getPlayer();

            ChatTextSection summary = new ChatTextSection();
            summary.append(SEPARATOR);
            summary.append(MessageUtils.formatLangMessage(Message.GAME_SUMMARY_BEDWARS, bukkit_player), TextAlignment.CENTER);
            summary.append(EMPTY);

            if (winner != null) {
                StringBuilder builder = new StringBuilder(winner.getColoredName());
                builder.append(ChatColor.GRAY).append(" - ");

                for (Player winner : winners)
                    builder.append(winner.getDisplayName()).append(", ");

                builder.delete(builder.length() - 2, builder.length());

                summary.append(builder.toString(), TextAlignment.CENTER);
                summary.append(EMPTY);
            }

            Iterator<Entry<Player, Integer>> iterator = top_players.entrySet().iterator();
            if (!top_players.isEmpty())
                summary.append(formatTopPlayer(Message.GAME_SUMMARY_TOP_PLAYERS_FIRST, iterator.next()), TextAlignment.CENTER);

            if (top_players.size() >= 2)
                summary.append(formatTopPlayer(Message.GAME_SUMMARY_TOP_PLAYERS_SECOND, iterator.next()), TextAlignment.CENTER);

            if (top_players.size() >= 3)
                summary.append(formatTopPlayer(Message.GAME_SUMMARY_TOP_PLAYERS_THIRD, iterator.next()), TextAlignment.CENTER);

            summary.append(EMPTY);
            summary.append(SEPARATOR);

            summary.sendSection(player.getPlayer());
        }

        private void appendExperienceSummary(@NotNull ChatTextSection summary, @NotNull GamePlayerRewardManager manager, @NotNull Player player) {
            User user = BedwarsUsersManager.getInstance().getUser(player);
            UserLevel level = user.getLevel();

            summary.append(ChatColor.AQUA + GameRewardType.BEDWARS_EXPERIENCE.getName(BedwarsUsersManager.getPlayerLanguage(player)), TextAlignment.CENTER);

            int exp = manager.getTotalAmount(GameRewardType.BEDWARS_EXPERIENCE);
            if (!level.isLeveling(exp)) {
                level.incrementProgress(exp, false);

                UserPrestige next = UserPrestige.getByStartLevel(level.getLevel() + 1);
                if (next != null)
                    summary.append(formatLevelToPrestigeLine(level.getLevel(), next, player), TextAlignment.CENTER);
                else
                    summary.append(formatLevelToLevelLine(level.getLevel(), level.getLevel() + 1, player), TextAlignment.CENTER);

                summary.append(level.getProgressBar(34), TextAlignment.CENTER);
                summary.append(level.getProgressTextWithComma() + formatPercent(level), TextAlignment.CENTER);
                return;
            }

            UserLevel leveled = level.clone();
            UserPrestige prestige = leveled.incrementProgress(exp, true);

            if (prestige != null) {
                summary.append(formatLevelToPrestigeLine(level.getLevel(), prestige, player), TextAlignment.CENTER);
                summary.append(PROGRESS_BAR, TextAlignment.CENTER);
                summary.append(formatLevelUp(prestige, player), TextAlignment.CENTER);

            } else {
                summary.append(formatLevelToLevelLine(level.getLevel(), leveled.getLevel(), player), TextAlignment.CENTER);
                summary.append(PROGRESS_BAR, TextAlignment.CENTER);
                summary.append(formatLevelUp(leveled, player), TextAlignment.CENTER);
            }

        }

        @NotNull
        private String formatTopPlayer(@NotNull Message message, @NotNull Entry<Player, Integer> entry) {
            Player player = entry.getKey();
            return MessageUtils.formatLangMessage(message, player, player.getDisplayName(), entry.getValue());
        }

        @NotNull
        private String formatLevelToLevelLine(int start, int end, @NotNull Player player) {
            String start_text = MessageUtils.formatLangMessage(Message.GAME_SUMMARY_REWARDS_LEVEL, player, start);
            String end_text = MessageUtils.formatLangMessage(Message.GAME_SUMMARY_REWARDS_LEVEL, player, end);

            int start_width = ChatTextUtils.getTextWidth(start_text);
            int end_width = ChatTextUtils.getTextWidth(end_text);

            return start_text + ChatTextUtils.getEmptyLine((244 - start_width - end_width) / 3) + end_text;
        }

        @NotNull
        private String formatLevelToPrestigeLine(int start, @NotNull UserPrestige end, @NotNull Player player) {
            String start_text = MessageUtils.formatLangMessage(Message.GAME_SUMMARY_REWARDS_LEVEL, player, start);
            return start_text + ChatTextUtils.getEmptyLine(35) + end.getDisplayName();
        }

        @NotNull
        private String formatLevelUp(@NotNull UserPrestige prestige, @NotNull Player player) {
            return MessageUtils.formatLangMessage(Message.GAME_SUMMARY_REWARDS_LEVEL_UP, player, prestige.getDisplayName());
        }

        @NotNull
        private String formatLevelUp(@NotNull UserLevel level, @NotNull Player player) {
            return MessageUtils.formatLangMessage(Message.GAME_SUMMARY_REWARDS_LEVEL_UP, player, NumberUtils.formatWithComma(level.getLevel()));
        }

        @NotNull
        private String formatPercent(@NotNull UserLevel level) {
            return ChatColor.GRAY + String.format(" (%.2f%%)", level.getProgressPercentage() * 100);
        }

        @NotNull
        private String format(@NotNull GameRewardType type, @NotNull GamePlayerRewardManager manager, @NotNull Player player) {
            return MessageUtils.formatLangMessage(Message.GAME_SUMMARY_REWARDS_EARNINGS, player, manager.getRewardText(type, BedwarsUsersManager.getPlayerLanguage(player)));
        }

        @NotNull
        private ChatText getRewardText(@NotNull GamePlayerRewardManager manager, @NotNull GameRewardType type, @NotNull Language lang) {
            TextComponent component = manager.getRewardHistoryText(type, lang);
            component.setText(CIRCLE + component.getText());

            return new ChatText(component);
        }

    }

    private final class GameTeamStorage {

        private final Collection<GamePlayerStorage> players;

        private final GameTeam team;
        private final Location spawn;

        public GameTeamStorage(@NotNull GameTeam team, @NotNull Collection<GamePlayerStorage> players, @NotNull Location spawn) {

            this.team = team;
            this.spawn = spawn;

            this.players = players;
        }

        @NotNull
        public Collection<GamePlayerStorage> getPlayers() {
            return players;
        }

        @NotNull
        public Collection<GamePlayer> getTeamPlayers() {
            Set<GamePlayer> result = new HashSet<>(players.size());

            for (GamePlayerStorage storage : players)
                result.add(storage.player);

            return result;
        }

        @NotNull
        public GameTeam getGameTeam() {
            return team;
        }

        @NotNull
        public Location getTeamSpawn() {
            return spawn;
        }

        public void remove() {
            Chest team_chest = arena.getTeam(team.getColor()).getChest();
            if (team_chest != null)
                team_chest.getBlockInventory().clear();

            this.team.getResourceGenerator().stop();
            this.players.clear();
        }

    }

    private final class GamePlayerStorage {

        public final GamePlayer player;
        private final GameTeamStorage team_storage;
        public boolean isDisconnected;
        public boolean isEliminated;
        public boolean isSpectator;

        public GamePlayerStorage(@NotNull GameTeamStorage team, @NotNull GamePlayer player) {
            this.team_storage = team;
            this.player = player;

            this.team_storage.players.add(this);
        }

        @NotNull
        public GameTeamStorage getTeamStorage() {
            return team_storage;
        }

        @NotNull
        public GamePlayer getGamePlayer() {
            return player;
        }

        public void respawn() {
            Player bukkit_player = player.getPlayer();

            TeamColor team_color = player.getTeamColor();
            Location team_spawn = team_storage.getTeamSpawn();

            PlayerUtils.resetPlayerHealth(bukkit_player);
            PlayerUtils.resetPlayerFood(bukkit_player);
            PlayerUtils.clear(bukkit_player);

            PlayerUtils.setPlayerListName(bukkit_player, team_color.getColoredRepresentingChar() + ' ' + bukkit_player.getDisplayName());
            PlayerUtils.setPlayerGameMode(bukkit_player, org.bukkit.GameMode.SURVIVAL);
            PlayerUtils.setPlayerFlight(bukkit_player, false, false);

            BedwarsTeamUtils.setPlayerArmor(bukkit_player, player.getArmorType(), team_color);

            bukkit_player.teleport(team_spawn); // Teleporting the player to the team's spawn-point
            bukkit_player.setCanPickupItems(true); // Make picking up items possible for the player

            // Adding permanent items to the player's inventory
            GamePlayerInventory game_inventory = player.getInventory();
            game_inventory.sendItems(bukkit_player);

            // Removing the invincibility state of the player
            Bukkit.getScheduler().runTaskLater(Bedwars.getInstance(), () -> player.setInvincible(Bedwars.getInstance(), false), 100L);

            this.isSpectator = false;
            this.team_storage.getGameTeam().getUpgradeManager().apply(player);

            // Calls the respawn event
            GamePlayerRespawnEvent event = new GamePlayerRespawnEvent(player);
            Bukkit.getPluginManager().callEvent(event);

            MessageUtils.sendEventMessage(event.getRespawnMessageHandler(), bukkit_player);
            Titles.sendTitle(bukkit_player, 5, 20, 5, MessageUtils.formatLangMessage(Message.PLAYER_RESPAWN_TITLE, bukkit_player), "");
        }

    }

    private final class GameWaitingRoomManager {

        private final WaitingRoom room = new BedwarsWaitingRoom(BedwarsGame.this);
        private List<BlockState> blocks;

        public GameWaitingRoomManager() {
        }

        @NotNull
        public WaitingRoom getWaitingRoom() {
            return room;
        }

        public void construct() {
            if (blocks == null)
                return;

            for (BlockState state : blocks)
                state.update(true, false);
        }

        public void destroy() {
            if (blocks != null)
                return;

            Region region = arena.getWaitingRoomRegion();

            World world = region.getWorld();
            int maxX = (int) region.getMaxX();
            int maxY = (int) region.getMaxY();
            int maxZ = (int) region.getMaxZ();

            int minX = (int) region.getMinX();
            int minY = (int) region.getMinY();
            int minZ = (int) region.getMinZ();

            List<BlockState> states = new ArrayList<>((maxX - minX) * (maxY - minY) * (maxZ - minZ));
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getType() == Material.AIR)
                            continue;

                        states.add(block.getState());
                        block.setType(Material.AIR);
                    }
                }
            }

            this.blocks = states;
        }

    }

    private final class GameSpectatorsManager {

        private final Map<UUID, BukkitTask> game_spectators = new HashMap<>(); // Game Players
        private final Set<UUID> added_spectators = new HashSet<>(); // Non-game Players

        private final Location spawn;

        public GameSpectatorsManager() {
            this.spawn = arena.getSpectatorSpawnLocation();
        }

        @NotNull
        public Collection<Player> getAddedSpectators() {
            Set<Player> players = new HashSet<>();

            for (UUID uuid : added_spectators) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null)
                    players.add(player);
            }

            return players;
        }

        @NotNull
        public Collection<Player> getGameSpectators() {
            Set<Player> players = new HashSet<>();

            for (UUID uuid : game_spectators.keySet()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null)
                    players.add(player);
            }

            return players;
        }

        public boolean addSpectator(@NotNull Player player) {
            if (player == null || available_players.containsKey(player.getUniqueId()))
                return false;

            if (!player.teleport(spawn))
                return false;

            PlayerUtils.setPlayerFlight(player, true, true);
            PlayerUtils.resetPlayerHealth(player);
            PlayerUtils.clear(player);

            player.teleport(spawn);
            player.setCanPickupItems(false);

            this.hideSpectator(player);
            return added_spectators.add(player.getUniqueId());
        }

        public boolean isSpectator(@NotNull Player player) {
            if (player == null)
                return false;

            if (added_spectators.contains(player.getUniqueId()))
                return true;

            GamePlayerStorage storage = available_players.get(player.getUniqueId());
            return storage != null && storage.isSpectator;
        }

        public void setSpectator(@NotNull Player player, int time) {
            if (time < 0) {
                this.setDefaultSpectator(player, true);
                return;
            }

            this.setDefaultSpectator(player, false);
            this.setRespawnSpectator(player, time);
        }

        public void setSpectator(@NotNull Player player) {
            this.setDefaultSpectator(player, true);
        }

        private void setDefaultSpectator(@NotNull Player player, boolean eliminated) {
            GamePlayerStorage storage = available_players.get(player.getUniqueId());
            storage.isEliminated = eliminated;
            storage.isSpectator = true;

            GamePlayer game_player = storage.getGamePlayer();
            game_player.setInvincible(Bedwars.getInstance(), true);

            PlayerUtils.setPlayerFlight(player, true, true);
            PlayerUtils.resetPlayerHealth(player);
            PlayerUtils.clear(player);

            player.teleport(spawn);
            player.setCanPickupItems(false);

            this.hideSpectator(player);
        }

        private void setRespawnSpectator(@NotNull Player player, int time) {
            BukkitTask existing = game_spectators.get(player.getUniqueId());
            if (existing != null)
                existing.cancel();

            this.game_spectators.put(player.getUniqueId(), new BukkitRunnable() {

                private int remaining = time;

                @Override
                public void run() {
                    if (isDisconnected(player)) {
                        this.cancel();
                        return;
                    }

                    if (remaining == 0) {
                        this.cancel();

                        GameSpectatorsManager.this.respawnSpectator(player);
                        GameSpectatorsManager.this.showSpectator(player);
                        return;
                    }

                    String death_title = MessageUtils.formatLangMessage(Message.PLAYER_DEATH_TITLE, player);
                    String respawn_message = MessageUtils.formatLangMessage(Message.PLAYER_RESPAWN_WAITING, player, remaining--);

                    Titles.sendTitle(player, 10, 20, 10, death_title, respawn_message);
                    player.sendMessage(respawn_message);
                }

            }.runTaskTimer(Bedwars.getInstance(), 0, 20L));

        }

        private void respawnSpectator(@NotNull Player player) {
            GamePlayerStorage storage = available_players.get(player.getUniqueId());
            if (storage != null)
                storage.respawn();
        }

        private void hideSpectator(@NotNull Player player) {
            Bedwars instance = Bedwars.getInstance();
            boolean isNewAPI = Version.getVersion().isNewAPI();

            for (Player other : spawn.getWorld().getPlayers()) {
                if (other.equals(player))
                    continue;

                if (isNewAPI)
                    other.hidePlayer(instance, player);
                else
                    other.hidePlayer(player);
            }
        }

        private void showSpectator(@NotNull Player player) {
            Bedwars instance = Bedwars.getInstance();
            boolean isNewAPI = Version.getVersion().isNewAPI();

            for (Player other : player.getWorld().getPlayers()) {
                if (other == player)
                    continue;

                if (isNewAPI)
                    other.showPlayer(instance, player);
                else
                    other.showPlayer(player);
            }

            this.game_spectators.remove(player.getUniqueId());
        }

        private void reset() {
            for (GamePlayerStorage storage : available_players.values())
                this.showSpectator(storage.getGamePlayer().getPlayer());
        }

    }

    private final class GameEnd {

        private final Map<Player, Integer> top_players = new LinkedHashMap<>(available_players.size(), 1F);

        private final Set<Player> winners;
        private final Set<Player> losers;

        private final GameSummary summary;
        private final TeamColor winner;

        public GameEnd() {
            List<Entry<Player, Integer>> players = new ArrayList<>(available_players.size());

            Set<Player> winners = new HashSet<>(available_teams.size() * mode.getTeamMax(), 1F);
            Set<Player> losers = new HashSet<>(eliminated_teams.size() * mode.getTeamMax(), 1F);

            TeamColor winner = available_teams.size() == 1 ? available_teams.keySet().iterator().next() : null;

            for (GamePlayerStorage storage : available_players.values()) {
                GamePlayer player = storage.getGamePlayer();
                Player bukkit_player = player.getPlayer();

                int kills = player.getStatisticManager().getStatistic(GamePlayerStatistic.KILLS);
                int final_kills = player.getStatisticManager().getStatistic(GamePlayerStatistic.FINAL_KILLS);

                players.add(new AbstractMap.SimpleEntry<>(player.getPlayer(), kills + final_kills));

                if (player.getTeamColor() == winner)
                    winners.add(bukkit_player);
                else
                    losers.add(bukkit_player);
            }

            players.sort(GameSummary.COMPARATOR);
            players.forEach(entry -> top_players.put(entry.getKey(), entry.getValue()));

            this.winners = winners;
            this.losers = losers;
            this.winner = winner;

            this.summary = new GameSummary(top_players, winners, winner);
        }

        public void stop(boolean forced) {
            BedwarsGame.this.spectators_manager.reset();

            for (GamePlayerStorage storage : available_players.values()) {
                this.sendRewards(storage, winner);
                this.remove(storage, forced);
            }

            if (!forced)
                Bukkit.getScheduler().runTaskLater(Bedwars.getInstance(), this::clear, 20 * 20L);
            else
                this.clear();

            GameEndEvent event = new GameEndEvent(BedwarsGame.this, top_players, winners, losers);
            Bukkit.getPluginManager().callEvent(event);
        }

        private void remove(@NotNull GamePlayerStorage storage, boolean forced) {
            if (storage.isDisconnected)
                return;

            GamePlayer player = storage.getGamePlayer();
            Player bukkit_player = player.getPlayer();
            bukkit_player.getEnderChest().clear();
            bukkit_player.closeInventory();

            if (player.getTeamColor() == winner)
                Titles.sendTitle(bukkit_player, 10, 100, 10, MessageUtils.formatLangMessage(Message.GAME_END_VICTORY_TITLE, bukkit_player), "");
            else
                Titles.sendTitle(bukkit_player, 10, 100, 10, MessageUtils.formatLangMessage(Message.GAME_END_DEFEAT_TITLE, bukkit_player), "");

            if (forced) {
                this.remove(bukkit_player);
                return;
            }

            Bukkit.getScheduler().runTaskLater(Bedwars.getInstance(), () -> this.remove(bukkit_player), 20 * 20L);
        }

        private void remove(@NotNull Player player) {
            BedwarsGame.DISCONNECTED_PLAYERS_GAMES.remove(player.getUniqueId(), BedwarsGame.this);
            BedwarsGame.CONNECTED_PLAYERS_GAMES.remove(player.getUniqueId(), BedwarsGame.this);
            BedwarsGameScoreboard.remove(player.getUniqueId());

            PlayerUtils.resetPlayerListName(player);
            PlayerUtils.resetPlayerFlight(player);
            PlayerUtils.resetPlayerHealth(player);
            PlayerUtils.resetPlayerFood(player);
            PlayerUtils.clear(player);

            player.setCanPickupItems(true);
            BedwarsLobbiesManager.getInstance().sendToRandomLobby(player);
        }

        private void sendRewards(@NotNull GamePlayerStorage storage, @Nullable TeamColor winner) {
            GamePlayer player = storage.getGamePlayer();
            OfflineUser user = BedwarsUsersManager.getInstance().getOfflineUser(player.getPlayer());

            UserStatistics stats = user.getStatistics(mode);
            stats.incrementStatistics(player.getStatisticManager());

            if (winner != null) {
                if (player.getTeamColor() != winner) {
                    stats.incrementStatistic(UserStatistic.LOSSES, 1);
                    stats.setStatistic(UserStatistic.WINSTREAK, 0);
                } else {
                    stats.incrementStatistic(UserStatistic.WINSTREAK, 1);
                    stats.incrementStatistic(UserStatistic.WINS, 1);

                    BedwarsGame.this.incrementReward(player, GameRewardReason.WIN);
                }

            }

            if (!storage.isDisconnected)
                this.summary.sendSummary(player);

            GamePlayerRewardManager manager = player.getRewardManager();

            UserLevel level = user.getLevel();
            UserPrestige prestige = level.incrementProgress(manager.getTotalAmount(GameRewardType.BEDWARS_EXPERIENCE), true);

            user.setPrestige(prestige);
            user.setLevel(level);

            UserWallet wallet = user.getWallet();
            for (UserCurrency currency : UserCurrency.values())
                wallet.incrementBalance(currency, manager.getTotalAmount(currency.getRewardType()));
        }

        private void clear() {
            BedwarsGame.this.spectators_manager.reset();
            BedwarsGame.this.available_players.clear();
        }
    }

}