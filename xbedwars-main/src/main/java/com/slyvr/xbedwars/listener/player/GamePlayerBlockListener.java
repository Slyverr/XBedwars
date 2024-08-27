package com.slyvr.xbedwars.listener.player;

import com.cryptomorin.xseries.XSound;
import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.api.arena.Arena;
import com.slyvr.xbedwars.api.arena.team.ArenaTeam;
import com.slyvr.xbedwars.api.arena.team.ArenaTeamBed;
import com.slyvr.xbedwars.api.event.player.block.GamePlayerBlockBreakEvent;
import com.slyvr.xbedwars.api.event.player.block.GamePlayerBlockPlaceEvent;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.generator.Resource;
import com.slyvr.xbedwars.api.generator.team.TeamResourceGenerator;
import com.slyvr.xbedwars.api.generator.tiered.TieredResourceGenerator;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.api.team.GameTeam;
import com.slyvr.xbedwars.game.XBedwarsGame;
import com.slyvr.xbedwars.settings.BedwarsGameSettings;
import com.slyvr.xbedwars.settings.BedwarsSettings;
import com.slyvr.xbedwars.utils.BedUtils;
import com.slyvr.xbedwars.utils.ListenerUtils;
import com.slyvr.xbedwars.utils.MessageUtils;
import com.slyvr.xbedwars.utils.Region;
import com.slyvr.xbedwars.utils.Version;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public final class GamePlayerBlockListener implements Listener {

    private static final MetadataValue EMPTY_METADATA = new FixedMetadataValue(XBedwars.getInstance(), null);

    private static final Map<Game, GameStorage> GAMES_STORAGES = new HashMap<>();
    private static final Effect CLOUD;

    static {
        CLOUD = !Version.getVersion().isNewAPI() && Version.getVersion() != Version.UNSUPPORTED ? Effect.valueOf("CLOUD") : null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onGamePlayerBlockBreak(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Game game = XBedwarsGame.getPlayerGame(player);
        if (game == null) {
            event.setCancelled(!BedwarsSettings.canPlayerDestroyBlock() && !player.hasPermission("bw.flags.block.break"));
            return;
        }

        if (!game.isRunning() || game.isSpectator(player)) {
            event.setCancelled(true);
            return;
        }

        if (BedUtils.isBed(block.getType())) {
            ArenaTeamBed bed = getArenaBed(game.getArena(), block);

            if (bed != null) {
                game.breakTeamBed(bed.getTeamColor(), player);
                event.setCancelled(true);
                return;
            }
        }

        if (!block.hasMetadata("xbedwars")) {
            player.sendMessage(ChatColor.RED + "You can only break blocks placed by players!");
            event.setCancelled(true);
            return;
        }

        if (block.hasMetadata("xbedwars-sponge")) {
            event.setCancelled(true);
            return;
        }

        GamePlayerBlockBreakEvent bwEvent = new GamePlayerBlockBreakEvent(game.getGamePlayer(player), block);
        Bukkit.getPluginManager().callEvent(bwEvent);

        if (bwEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        GamePlayerBlockListener.removePlacedBlock(game, block);
    }

    @EventHandler(ignoreCancelled = true)
    public void onGamePlayerBlockPlace(@NotNull BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Game game = XBedwarsGame.getPlayerGame(player);
        if (game == null) {
            event.setCancelled(!BedwarsSettings.canPlayerPlaceBlock() && !player.hasPermission("bw.flags.block-place"));
            return;
        }

        if (!game.isRunning() || game.isSpectator(player)) {
            event.setCancelled(true);
            return;
        }

        Region region = game.getArena().getRegion();
        if (block.getY() > region.getMaxY()) {
            MessageUtils.sendLangMessage(Message.BLOCK_PLACE_RESTRICTED_HEIGHT_LIMIT, player);
            event.setCancelled(true);
            return;
        }

        if (!region.isInside(block)) {
            MessageUtils.sendLangMessage(Message.BLOCK_PLACE_RESTRICTED, player);
            event.setCancelled(true);
            return;
        }

        // Tiered Resource Generator
        for (Resource resource : Resource.values()) {
            Collection<TieredResourceGenerator> generators = game.getResourceGenerators(resource);
            if (generators.isEmpty())
                continue;

            for (TieredResourceGenerator generator : generators) {
                if (generator.getDropLocation().getBlock().getLocation().distanceSquared(block.getLocation()) > 9)
                    continue;

                MessageUtils.sendLangMessage(Message.BLOCK_PLACE_RESTRICTED, player);
                event.setCancelled(true);
                return;
            }
        }

        // Team Resource Generator
        for (GameTeam team : game.getGameTeams()) {
            TeamResourceGenerator generator = team.getResourceGenerator();
            if (generator.getDropLocation().getBlock().getLocation().distanceSquared(block.getLocation()) > 9)
                continue;

            MessageUtils.sendLangMessage(Message.BLOCK_PLACE_RESTRICTED, player);
            event.setCancelled(true);
            return;
        }

        GamePlayer game_player = game.getGamePlayer(player);

        GamePlayerBlockPlaceEvent bwEvent = new GamePlayerBlockPlaceEvent(game_player, block);
        Bukkit.getPluginManager().callEvent(bwEvent);

        if (bwEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        if (block.getType() == Material.TNT) {
            this.handleGamePlayerTNTPlacement(game_player, event);
            return;
        }

        if (block.getType() == Material.SPONGE) {
            this.handleGamePlayerSpongePlacement(game_player, event);
            return;
        }

        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            this.handleGamePlayerPopUpTowerPlacement(game_player, event);
            return;
        }

        GamePlayerBlockListener.addPlacedBlock(game, block);
    }

    @Nullable
    private ArenaTeamBed getArenaBed(@NotNull Arena arena, @NotNull Block block) {
        for (ArenaTeam team : arena.getReadyTeams()) {
            ArenaTeamBed bed = team.getBed();
            if (bed == null)
                continue;

            if (bed.getHead().equals(block) || bed.getFoot().equals(block))
                return bed;
        }

        return null;
    }

    private void handleGamePlayerTNTPlacement(@NotNull GamePlayer player, @NotNull BlockPlaceEvent event) {
        Location loc = event.getBlock().getLocation().add(0.5, 0, 0.5);

        TNTPrimed tnt = (TNTPrimed) loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
        tnt.setMetadata("xbedwars-explosive", new FixedMetadataValue(XBedwars.getInstance(), player));
        tnt.setIsIncendiary(BedwarsGameSettings.isTNTIncendiary());
        tnt.setFuseTicks(BedwarsGameSettings.getTNTExplosionTime());
        tnt.setYield(BedwarsGameSettings.getTNTExplosionPower());

        if (BedwarsGameSettings.showTNTExplosionTime()) {
            tnt.setCustomNameVisible(true);

            new BukkitRunnable() {

                private int ticks = tnt.getFuseTicks();

                @Override
                public void run() {
                    if (tnt.isDead()) {
                        this.cancel();
                        return;
                    }

                    tnt.setCustomName((ticks < 40 ? (ticks >= 20 ? ChatColor.GOLD : ChatColor.RED) : ChatColor.GREEN) + Integer.toString(--ticks));
                }

            }.runTaskTimerAsynchronously(XBedwars.getInstance(), 0, 1);

        }

        ListenerUtils.decrementItemInHandAmount(event.getPlayer(), isRightHand(event));
        event.setCancelled(true);
    }

    private void handleGamePlayerPopUpTowerPlacement(@NotNull GamePlayer player, @NotNull BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    private void handleGamePlayerSpongePlacement(@NotNull GamePlayer player, @NotNull BlockPlaceEvent event) {
        Block block = event.getBlock();
        block.setMetadata("xbedwars-sponge", EMPTY_METADATA);

        new BukkitRunnable() {
            private final Location center = block.getLocation().add(0.5, 0.5, 0.5);

            private final int particles_maximum_radius = BedwarsGameSettings.getSpongeParticlesRadius();
            private final int particles_growth_rate = BedwarsGameSettings.getSpongeParticlesRate();

            private int particles_radius = 1;
            private int particles_count = 200;

            @Override
            public void run() {
                if (particles_radius > particles_maximum_radius) {
                    this.cancel();

                    block.setType(Material.AIR);
                    block.removeMetadata("xbedwars-sponge", XBedwars.getInstance());

                    GamePlayerBlockListener.removePlacedBlock(player.getGame(), block);
                    return;
                }

                float offset = particles_radius * 0.33F;
                GamePlayerBlockListener.this.spawnCloud(center, particles_count, offset, offset, offset);
                GamePlayerBlockListener.this.spawnCloudSound(center);

                this.particles_radius += 1;
                this.particles_count *= particles_growth_rate;
            }

        }.runTaskTimer(XBedwars.getInstance(), 2, 10L);

        GamePlayerBlockListener.addPlacedBlock(player.getGame(), block);
    }

    private void spawnCloud(@NotNull Location loc, int count, float offsetX, float offsetY, float offsetZ) {
        if (!Version.getVersion().isNewAPI()) {
            ListenerUtils.playEffect(CLOUD, loc, count, offsetX, offsetY, offsetZ, 0F);
            return;
        }

        loc.getWorld().spawnParticle(Particle.CLOUD, loc, count, offsetX, offsetY, offsetZ, 0F);
    }

    private void spawnCloudSound(@NotNull Location loc) {
        XSound.BLOCK_NOTE_BLOCK_HAT.play(loc, 1F, 1F);
    }

    private boolean isRightHand(@NotNull BlockPlaceEvent event) {
        return !Version.getVersion().isNewerThan(Version.V1_8_R3) || event.getHand() != EquipmentSlot.OFF_HAND;
    }

    public static void spawnEffect() {

    }

    // Map Blocks
    public static void addDestroyedMapBlocks(@NotNull Game game, @NotNull Collection<Block> blocks) {
        GameStorage storage = GAMES_STORAGES.computeIfAbsent(game, (param) -> new GameStorage());
        storage.addDestroyedMapBlocks(blocks);
    }

    public static void removeDestroyedMapBlocks(@NotNull Game game, @NotNull Collection<Block> blocks) {
        GameStorage storage = GAMES_STORAGES.computeIfAbsent(game, (param) -> new GameStorage());
        storage.removeDestroyedMapBlocks(blocks);
    }

    public static void addDestroyedMapBlock(@NotNull Game game, @NotNull Block block) {
        GameStorage storage = GAMES_STORAGES.computeIfAbsent(game, (param) -> new GameStorage());
        storage.addDestroyedMapBlock(block);
    }

    public static void removeDestroyedMapBlock(@NotNull Game game, @NotNull Block block) {
        GameStorage storage = GAMES_STORAGES.computeIfAbsent(game, (param) -> new GameStorage());
        storage.removeDestroyedMapBlock(block);
    }

    public static void addPlacedBlocks(@NotNull Game game, @NotNull Collection<Block> blocks) {
        GameStorage storage = GAMES_STORAGES.computeIfAbsent(game, (param) -> new GameStorage());
        storage.addPlacedBlocks(blocks);
    }

    public static void removePlacedBlocks(@NotNull Game game, @NotNull Collection<Block> blocks) {
        GameStorage storage = GAMES_STORAGES.computeIfAbsent(game, (param) -> new GameStorage());
        storage.removePlacedBlocks(blocks);
    }

    public static void addPlacedBlock(@NotNull Game game, @NotNull Block block) {
        GameStorage storage = GAMES_STORAGES.computeIfAbsent(game, (param) -> new GameStorage());
        storage.addPlacedBlock(block);
    }

    public static void removePlacedBlock(@NotNull Game game, @NotNull Block block) {
        GameStorage storage = GAMES_STORAGES.computeIfAbsent(game, (param) -> new GameStorage());
        storage.removePlacedBlock(block);
    }

    public static void reset(@NotNull Game game) {
        GameStorage storage = GAMES_STORAGES.get(game);
        if (storage == null)
            return;

        storage.removePlacedBlocks();
        storage.restoreDestroyedBlocks();
    }

    private static final class GameStorage {

        private final Map<Block, BlockState> destroyed_blocks = new HashMap<>(); // Map blocks destroyed by players.
        private final Set<Block> placed_blocks = new HashSet<>(); // Block placed by players.

        public GameStorage() {
        }

        public void addPlacedBlocks(@NotNull Collection<Block> blocks) {
            for (Block block : blocks)
                this.addPlacedBlock(block);
        }

        public void removePlacedBlocks(@NotNull Collection<Block> blocks) {
            for (Block block : blocks)
                this.removePlacedBlock(block);
        }

        public void addPlacedBlock(@NotNull Block block) {
            if (placed_blocks.add(block))
                block.setMetadata("xbedwars", EMPTY_METADATA);
        }

        public void removePlacedBlock(@NotNull Block block) {
            if (placed_blocks.remove(block))
                block.removeMetadata("xbedwars", XBedwars.getInstance());
        }

        public void addDestroyedMapBlocks(@NotNull Collection<Block> blocks) {
            for (Block block : blocks)
                this.addDestroyedMapBlock(block);
        }

        public void removeDestroyedMapBlocks(@NotNull Collection<Block> blocks) {
            for (Block block : blocks)
                this.removeDestroyedMapBlock(block);
        }

        public void addDestroyedMapBlock(@NotNull Block block) {
            this.destroyed_blocks.putIfAbsent(block, block.getState());
        }

        public void removeDestroyedMapBlock(@NotNull Block block) {
            this.destroyed_blocks.remove(block);
        }


        public void restoreDestroyedBlocks() {
            for (BlockState state : destroyed_blocks.values())
                state.update(true, false);

        }

        public void removePlacedBlocks() {
            for (Block block : placed_blocks) {
                if (block.getType() != Material.AIR)
                    block.setType(Material.AIR);

                block.removeMetadata("xbedwars", XBedwars.getInstance());
            }

            this.placed_blocks.clear();
        }

    }

}