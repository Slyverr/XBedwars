package com.slyvr.xbedwars.listener.mechanics;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.game.XBedwarsGame;
import com.slyvr.xbedwars.listener.player.GamePlayerBlockListener;
import com.slyvr.xbedwars.settings.BedwarsGameSettings;
import com.slyvr.xbedwars.settings.BedwarsSettings;
import com.slyvr.xbedwars.team.BedwarsTeamUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public final class GameMechanicsListener implements Listener {

    private static final MetadataValue EMPTY_METADATA = new FixedMetadataValue(XBedwars.getInstance(), null);

    public GameMechanicsListener() {
    }

    @EventHandler(ignoreCancelled = true)
    public void onGamePlayerExplosiveExplosion(@NotNull EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Explosive))
            return;

        Explosive explosive = (Explosive) entity;
        if (!explosive.hasMetadata("xbedwars-explosive")) {
            event.setCancelled(!BedwarsSettings.canEntityGrief());
            return;
        }

        GamePlayer owner = (GamePlayer) explosive.getMetadata("xbedwars-explosive").get(0).value();
        event.blockList().removeIf(block -> !block.hasMetadata("xbedwars") || isStainedGlass(block));

        List<Entity> nearby_entities = explosive.getNearbyEntities(explosive.getYield(), explosive.getYield(), explosive.getYield());
        if (nearby_entities.isEmpty())
            return;

        Location explosive_loc = explosive.getLocation();
        Vector explosive_vec = explosive_loc.toVector();

        double radius = explosive.getYield();
        double radius_squared = radius * radius;

        for (Entity nearby : nearby_entities) {
            if (nearby instanceof Item)
                continue;

            Location nearby_loc = nearby.getLocation();
            Vector nearby_vec = nearby_loc.toVector();

            double distance_squared = nearby_loc.distanceSquared(explosive_loc);
            if (distance_squared <= 0.25) {
                nearby.setVelocity(new Vector(0, 1, 0));
                continue;
            }

            Vector direction = nearby_vec.subtract(explosive_vec).normalize();
            direction.setX(direction.getX() * radius);
            direction.setY(direction.getY() * radius);
            direction.setZ(direction.getZ() * radius);

            nearby.setVelocity(direction.multiply(0.45F));
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onGamePlayerBridgeEggThrow(@NotNull ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        ProjectileSource source = projectile.getShooter();

        if (!(projectile instanceof Egg) || !(source instanceof Player))
            return;

        Player bukkit_player = (Player) source;

        Game game = XBedwarsGame.getPlayerGame(bukkit_player);
        if (game == null || !game.isRunning() || game.isSpectator(bukkit_player))
            return;

        GamePlayer player = game.getGamePlayer(bukkit_player);
        if (player == null)
            return;

        BridgeEggTask task = new BridgeEggTask(player, (Egg) projectile);
        task.runTaskTimer(XBedwars.getInstance(), 2L, 1L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onGamePlayerBridgeEggHit(@NotNull PlayerEggThrowEvent event) {
        if (event.getEgg().hasMetadata("xbedwars-egg"))
            event.setHatching(false);
    }

    private boolean isStainedGlass(@NotNull Block block) {
        return block.getType().toString().endsWith("STAINED_GLASS");
    }

    private static final class BridgeEggTask extends BukkitRunnable {

        private static final XMaterial BRIDGE_BLOCK_TYPE = BedwarsGameSettings.getBridgeBlockType();
        private static final int MAXIMUM_LENGTH = BedwarsGameSettings.getBridgeLength();
        private static final int MAXIMUM_WIDTH = BedwarsGameSettings.getBridgeWidth();

        private final XMaterial type;

        private final Game game;
        private final Egg egg;

        private int length;

        public BridgeEggTask(@NotNull GamePlayer player, @NotNull Egg egg) {
            this.type = BedwarsTeamUtils.getColoredBlock(BRIDGE_BLOCK_TYPE, player.getTeamColor());

            this.game = player.getGame();
            this.egg = egg;

            this.egg.setMetadata("xbedwars-egg", EMPTY_METADATA);
        }

        @Override
        public void run() {
            if (!egg.isValid() || length >= MAXIMUM_LENGTH) {
                this.cancel();
                return;
            }

            Location egg_location = egg.getLocation();
            if (!game.getArena().getRegion().isInside(egg_location))
                return;

            Block center = egg_location.getBlock();

            Bukkit.getScheduler().runTaskLater(XBedwars.getInstance(), () -> {
                BridgeEggTask.this.placeBlock(center, BlockFace.SELF, 1);

                for (int i = 1; i <= MAXIMUM_WIDTH; i++) {
                    BridgeEggTask.this.placeBlock(center, BlockFace.NORTH, i);
                    BridgeEggTask.this.placeBlock(center, BlockFace.SOUTH, i);
                    BridgeEggTask.this.placeBlock(center, BlockFace.EAST, i);
                    BridgeEggTask.this.placeBlock(center, BlockFace.WEST, i);
                }

                this.length++;
                XSound.ENTITY_EGG_THROW.play(egg_location, 1F, 0.5F);
            }, 2L);

        }

        private void placeBlock(@NotNull Block center, @NotNull BlockFace direction, int distance) {
            Block target = center.getRelative(direction, distance);
            if (target.getType() != Material.AIR)
                return;

            XBlock.setType(target, type);
            GamePlayerBlockListener.addPlacedBlock(game, target);
        }

    }

}