package com.slyvr.xbedwars.listener.entity;

import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.api.entity.GameEntity;
import com.slyvr.xbedwars.api.entity.GameEntityType;
import com.slyvr.xbedwars.api.entity.GameEntityType.SpawnType;
import com.slyvr.xbedwars.api.event.entity.GameEntityDamageByGamePlayerEvent;
import com.slyvr.xbedwars.api.event.entity.GameEntityDamageEvent;
import com.slyvr.xbedwars.api.event.entity.GameEntitySpawnEvent;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.api.team.GameTeam;
import com.slyvr.xbedwars.api.user.level.UserLevel;
import com.slyvr.xbedwars.settings.BedwarsGameSettings;
import com.slyvr.xbedwars.utils.MessageUtils;
import com.slyvr.xbedwars.utils.Version;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public final class GameEntityListener implements Listener {

    private static final Map<GameTeam, GameTeamEntitiesStorage> ENTITIES_STORAGE = new HashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onGameEntityDamage(@NotNull EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player))
            this.handleGameEntityDamage(event, event.getEntity(), event.getDamager(), event.getCause(), event.getDamage());
    }

    @EventHandler(ignoreCancelled = true)
    public void onGameEntityDamage(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player) && !isEntityDamageCause(event.getCause()))
            this.handleGameEntityDamage(event, event.getEntity(), null, event.getCause(), event.getDamage());
    }

    @EventHandler(ignoreCancelled = true)
    public void onGameEntitySpawn(@NotNull EntitySpawnEvent event) {
        GameEntity entity = GameEntityType.getGameEntity(event.getEntity());
        if (entity == null)
            return;

        GameEntitySpawnEvent bwEvent = new GameEntitySpawnEvent(entity);
        Bukkit.getPluginManager().callEvent(bwEvent);

        if (bwEvent.isCancelled())
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onGameEntityDeath(@NotNull EntityDeathEvent event) {
        GameEntity entity = GameEntityType.getGameEntity(event.getEntity());
        if (entity != null)
            GameEntityListener.remove(entity);
    }

    @EventHandler(ignoreCancelled = true)
    public void onGameEntitySpawn(@NotNull ProjectileHitEvent event) {
        if (event.getEntityType() != EntityType.SNOWBALL)
            return;

        Projectile projectile = event.getEntity();

        List<MetadataValue> owner_values = projectile.getMetadata("xbedwars-entity-owner");
        if (owner_values.isEmpty())
            return;

        Object owner_data = owner_values.get(0).value();
        if (!(owner_data instanceof GamePlayer))
            return;

        List<MetadataValue> type_values = projectile.getMetadata("xbedwars-entity-type");
        if (type_values.isEmpty())
            return;

        Object type_data = type_values.get(0).value();
        if (!(type_data instanceof GameEntityType))
            return;

        GamePlayer owner = (GamePlayer) owner_data;
        GameEntityType<?> type = (GameEntityType<?>) type_data;

        if (GameEntityListener.spawn(owner, type, projectile.getLocation()))
            return;

        if (type.getSpawnType() != SpawnType.PROJECTILE)
            return;

        owner.getPlayer().getInventory().addItem(type.getSpawnItem());
        MessageUtils.sendLangMessage(Message.GAME_ENTITY_SPAWN_RESTRICTED_LIMIT, owner.getPlayer());
    }

    private void handleGameEntityDamage(@NotNull EntityDamageEvent event, @NotNull Entity entity, @Nullable Entity damager, @NotNull DamageCause cause, double damage) {
        GameEntity damaged = GameEntityType.getGameEntity(entity);
        if (damaged == null)
            return;

        if (cause == DamageCause.VOID) {
            GameEntityListener.remove(damaged);
            event.setCancelled(true);
            return;
        }

        GameEntityDamageEvent bwEvent = callGameEntityDamageEvent(damaged, damager, cause, damage);
        if (bwEvent == null || bwEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        event.setDamage(bwEvent.getDamage());
    }

    @Nullable
    private GameEntityDamageEvent callGameEntityDamageEvent(@NotNull GameEntity damaged, @Nullable Entity damager, @NotNull DamageCause cause, double damage) {
        if (!(damager instanceof LivingEntity)) {
            GameEntityDamageEvent event = new GameEntityDamageEvent(damaged, cause, damage);
            Bukkit.getPluginManager().callEvent(event);

            return event;
        }

        if (damager instanceof Player) {
            GamePlayer game_player = damaged.getOwner().getGame().getGamePlayer((Player) damager);
            if (game_player == null || game_player.getTeamColor() == damaged.getOwner().getTeamColor())
                return null;

            if (game_player.getGame().isSpectator((Player) damager))
                return null;

            GameEntityDamageByGamePlayerEvent event = new GameEntityDamageByGamePlayerEvent(damaged, game_player, cause, damage);
            Bukkit.getPluginManager().callEvent(event);
            return event;
        }

        GameEntity game_entity = GameEntityType.getGameEntity(damager);
        if (game_entity != null) {
            if (game_entity.getOwner().getTeamColor() == damaged.getOwner().getTeamColor())
                return null;

            GameEntityDamageByGamePlayerEvent event = new GameEntityDamageByGamePlayerEvent(damaged, game_entity.getOwner(), cause, damage);
            Bukkit.getPluginManager().callEvent(event);
            return event;
        }


        return null;
    }

    public static boolean spawn(@NotNull GamePlayer player, @NotNull GameEntityType<?> type, @NotNull Location loc) {
        GameTeamEntitiesStorage storage = ENTITIES_STORAGE.computeIfAbsent(player.getTeam(), key -> new GameTeamEntitiesStorage());
        return storage.spawn(player, type, loc);
    }

    public static boolean launch(@NotNull GamePlayer player, @NotNull GameEntityType<?> type) {
        GameTeamEntitiesStorage storage = ENTITIES_STORAGE.computeIfAbsent(player.getTeam(), key -> new GameTeamEntitiesStorage());
        return storage.launch(player, type);
    }

    public static boolean canSpawn(@NotNull GamePlayer player, @NotNull GameEntityType<?> type) {
        GameTeamEntitiesStorage storage = ENTITIES_STORAGE.computeIfAbsent(player.getTeam(), key -> new GameTeamEntitiesStorage());
        return storage.canSpawn(type);
    }

    private static boolean isEntityDamageCause(@NotNull DamageCause cause) {
        if (cause == DamageCause.ENTITY_ATTACK)
            return true;

        if (cause == DamageCause.ENTITY_EXPLOSION)
            return true;

        return Version.getVersion().isNewAPI() && cause == DamageCause.ENTITY_SWEEP_ATTACK;
    }

    private static void remove(@NotNull GameEntity entity) {
        GameTeamEntitiesStorage storage = ENTITIES_STORAGE.get(entity.getOwner().getTeam());
        if (storage != null)
            storage.remove(entity);
        else
            entity.remove();
    }

    private static final class GameTeamEntitiesStorage {

        public final Map<GameEntityType, Integer> entities_count = new HashMap<>();

        public GameTeamEntitiesStorage() {
        }

        public boolean spawn(@NotNull GamePlayer player, @NotNull GameEntityType<?> type, @NotNull Location loc) {
            if (!canSpawn(type))
                return false;

            int duration = BedwarsGameSettings.getLifeDuration(type);
            if (duration <= 0)
                return true;

            int health = BedwarsGameSettings.getHealth(type);
            if (health <= 0)
                return true;

            GameEntity entity = GameEntityType.create(type, player, loc);

            LivingEntity bukkit_entity = (LivingEntity) entity.spawn();
            bukkit_entity.setCustomNameVisible(true);
            bukkit_entity.setMaxHealth(health);
            bukkit_entity.setHealth(health);

            BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(XBedwars.getInstance(), new Runnable() {
                private final ChatColor color = player.getTeamColor().getChatColor();
                private float left = duration;

                @Override
                public void run() {
                    if (bukkit_entity.isDead() || left == 0)
                        return;

                    bukkit_entity.setCustomName(color + String.format("%.1fs ", left -= 0.1F) + getHealth());
                }

                @NotNull
                public String getHealth() {
                    return UserLevel.getProgressBar(10, (float) (bukkit_entity.getHealth() / bukkit_entity.getMaxHealth()), color);
                }

            }, 0L, 2L);

            Bukkit.getScheduler().runTaskLater(XBedwars.getInstance(), () -> {
                this.remove(entity);
                task.cancel();
            }, duration * 20L);

            this.entities_count.put(type, entities_count.getOrDefault(type, 0) + 1);
            MessageUtils.sendLangMessage(Message.GAME_ENTITY_SPAWN, player.getPlayer(), duration);
            return true;
        }

        public boolean launch(@NotNull GamePlayer player, @NotNull GameEntityType<?> type) {
            if (!canSpawn(type))
                return false;

            Projectile projectile = player.getPlayer().launchProjectile(Snowball.class);
            projectile.setMetadata("xbedwars-entity-owner", new FixedMetadataValue(XBedwars.getInstance(), player));
            projectile.setMetadata("xbedwars-entity-type", new FixedMetadataValue(XBedwars.getInstance(), type));
            return true;
        }

        public void remove(@NotNull GameEntity entity) {
            entities_count.computeIfPresent(entity.getGameEntityType(), (key, value) -> value >= 1 ? value - 1 : 0);
            entity.remove();
        }

        public boolean canSpawn(@NotNull GameEntityType<?> type) {
            int limit = BedwarsGameSettings.getLimit(type);
            if (limit < 0)
                return true;

            int count = entities_count.getOrDefault(type, 0);
            return count < limit;
        }

    }

}