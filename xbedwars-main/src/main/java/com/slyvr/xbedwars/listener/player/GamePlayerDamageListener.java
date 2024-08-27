package com.slyvr.xbedwars.listener.player;

import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.api.boss.GameBoss;
import com.slyvr.xbedwars.api.boss.GameBossType;
import com.slyvr.xbedwars.api.entity.GameEntity;
import com.slyvr.xbedwars.api.entity.GameEntityType;
import com.slyvr.xbedwars.api.event.player.damage.GamePlayerDamageByGameBossEvent;
import com.slyvr.xbedwars.api.event.player.damage.GamePlayerDamageByGameEntityEvent;
import com.slyvr.xbedwars.api.event.player.damage.GamePlayerDamageByGamePlayerEvent;
import com.slyvr.xbedwars.api.event.player.damage.GamePlayerDamageEvent;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.game.XBedwarsGame;
import com.slyvr.xbedwars.settings.BedwarsGameSettings;
import com.slyvr.xbedwars.settings.BedwarsSettings;
import com.slyvr.xbedwars.utils.Version;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class GamePlayerDamageListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onGamePlayerDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player)
            this.handleGamePlayerDamage(event, (Player) event.getEntity(), event.getDamager(), event.getCause(), event.getFinalDamage());
    }

    @EventHandler(ignoreCancelled = true)
    public void onGamePlayerDamage(@NotNull EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && isValidDamageCause(event.getCause()))
            this.handleGamePlayerDamage(event, (Player) event.getEntity(), null, event.getCause(), event.getFinalDamage());
    }

    @EventHandler
    public void onGamePlayerVoid(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player) || event.getCause() != DamageCause.VOID)
            return;

        Player player = (Player) event.getEntity();
        Game game = XBedwarsGame.getPlayerGame(player);

        if (game == null) {
            event.setCancelled(!BedwarsSettings.canPlayerTakeDamage());
            return;
        }

        Bukkit.getScheduler().runTask(XBedwars.getInstance(), () -> {
            if (!game.isRunning() || game.isSpectator(player))
                player.teleport(game.getArena().getSpectatorSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            else
                game.killPlayer(player, player.getKiller(), DamageCause.VOID);

        });

        event.setCancelled(true);
    }

    private void handleGamePlayerDamage(@NotNull EntityDamageEvent event, @NotNull Player player, @Nullable Entity damager, @NotNull DamageCause cause, double damage) {
        Game game = XBedwarsGame.getPlayerGame(player);
        if (game == null) {
            event.setCancelled(!BedwarsSettings.canPlayerTakeDamage());
            return;
        }

        if (!game.isRunning() || game.isSpectator(player)) {
            event.setCancelled(true);
            return;
        }

        GamePlayer damaged = game.getGamePlayer(player);
        if (damaged.isInvincible()) {
            event.setCancelled(true);
            return;
        }

        GamePlayerDamageEvent bwEvent = callGamePlayerDamageEvent(damaged, damager, cause, damage);
        if (bwEvent == null || bwEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        damaged.setInvisible(XBedwars.getInstance(), false);

        event.setDamage(bwEvent.getDamage());
        if (player.getHealth() - bwEvent.getDamage() > 0)
            return;

        game.killPlayer(player, damager, bwEvent.getCause());
        event.setCancelled(true);
    }

    @Nullable
    private GamePlayerDamageEvent callGamePlayerDamageEvent(@NotNull GamePlayer damaged, @Nullable Entity damager, @NotNull DamageCause cause, double damage) {
        if (damager == null) {
            GamePlayerDamageEvent event = new GamePlayerDamageEvent(damaged, cause, damage);
            Bukkit.getPluginManager().callEvent(event);

            return event;
        }

        if (damager instanceof Player) {
            Player player = (Player) damager;
            GamePlayer game_player = damaged.getGame().getGamePlayer(player);
            if (game_player == null || game_player.getTeamColor() == damaged.getTeamColor())
                return null;

            if (game_player.getGame().isSpectator(player))
                return null;

            if (game_player.isInvincible())
                game_player.setInvincible(XBedwars.getInstance(), false);

            GamePlayerDamageByGamePlayerEvent event = new GamePlayerDamageByGamePlayerEvent(damaged, game_player, cause, damage);
            Bukkit.getPluginManager().callEvent(event);

            return event;
        }

        GameEntity game_entity = GameEntityType.getGameEntity(damager);
        if (game_entity != null) {
            if (game_entity.getOwner().getTeamColor() == damaged.getTeamColor())
                return null;

            GamePlayerDamageByGameEntityEvent event = new GamePlayerDamageByGameEntityEvent(damaged, game_entity, cause, getEntityDamage(game_entity, damage));
            Bukkit.getPluginManager().callEvent(event);

            return event;
        }

        GameBoss game_boss = GameBossType.getGameBoss(damager);
        if (game_boss != null) {
            if (game_boss.getGameTeam().getColor() == damaged.getTeamColor())
                return null;

            GamePlayerDamageByGameBossEvent event = new GamePlayerDamageByGameBossEvent(damaged, game_boss, cause, damage);
            Bukkit.getPluginManager().callEvent(event);

            return event;
        }

        GamePlayerDamageEvent event = new GamePlayerDamageEvent(damaged, cause, getExplosiveDamage(damager, damage));
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    private double getEntityDamage(@NotNull GameEntity entity, double def) {
        int damage = BedwarsGameSettings.getDamage(entity.getGameEntityType());
        return damage != 0 ? damage : def;
    }

    private double getExplosiveDamage(@NotNull Entity entity, double def) {
        if (!(entity instanceof Explosive))
            return def;

        Explosive explosive = (Explosive) entity;
        switch (explosive.getType()) {
            case PRIMED_TNT:
            case MINECART_TNT:
                return BedwarsGameSettings.getTNTExplosionDamage();

            case FIREBALL:
            case DRAGON_FIREBALL:
            case SMALL_FIREBALL:
                return BedwarsGameSettings.getFireballExplosionDamage();

            default:
                return def;
        }

    }

    private boolean isValidDamageCause(@NotNull DamageCause cause) {
        switch (cause) {
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
            case ENTITY_ATTACK:
            case VOID:
                return false;
            default:
                break;
        }

        return !Version.getVersion().isNewAPI() || cause != DamageCause.ENTITY_SWEEP_ATTACK;
    }

}
