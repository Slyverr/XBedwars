package com.slyvr.xbedwars.listener.boss;

import com.slyvr.xbedwars.api.boss.GameBoss;
import com.slyvr.xbedwars.api.boss.GameBossType;
import com.slyvr.xbedwars.api.entity.GameEntity;
import com.slyvr.xbedwars.api.entity.GameEntityType;
import com.slyvr.xbedwars.api.event.boss.GameBossDamageByGamePlayerEvent;
import com.slyvr.xbedwars.api.event.boss.GameBossSpawnEvent;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.game.XBedwarsGame;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.jetbrains.annotations.NotNull;


public final class GameBossListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onGameBossDamageByGamePlayer(@NotNull EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player || !(event.getDamager() instanceof Player))
            return;

        GameBoss damaged = GameBossType.getGameBoss(event.getEntity());
        if (damaged == null)
            return;

        Player player = (Player) event.getDamager();
        Game game = XBedwarsGame.getPlayerGame(player);

        if (game == null || game.isSpectator(player)) {
            event.setCancelled(true);
            return;
        }

        GamePlayer gp = game.getGamePlayer(player);
        if (gp.getTeamColor() == damaged.getGameTeam().getColor()) {
            event.setCancelled(true);
            return;
        }

        GameBossDamageByGamePlayerEvent bwEvent = new GameBossDamageByGamePlayerEvent(damaged, gp, event.getFinalDamage());
        Bukkit.getPluginManager().callEvent(bwEvent);

        if (bwEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        event.setDamage(bwEvent.getDamage());
    }

    @EventHandler(ignoreCancelled = true)
    public void onGameBossTarget(@NotNull EntityTargetLivingEntityEvent event) {
        GameBoss boss = GameBossType.getGameBoss(event.getEntity());
        if (boss == null)
            return;

        Entity target = event.getTarget();
        if (target instanceof Player) {
            GamePlayer player = boss.getGameTeam().getGame().getGamePlayer((Player) target);
            if (player != null && player.getTeamColor() != boss.getGameTeam().getColor())
                return;

            event.setCancelled(true);
            return;
        }

        GameEntity entity = GameEntityType.getGameEntity(target);
        if (entity == null || entity.getOwner().getTeamColor() == boss.getGameTeam().getColor()) {
            event.setCancelled(true);
            return;
        }

        GameBoss target_boss = GameBossType.getGameBoss(target);
        if (target_boss != null) {
            event.setCancelled(true);
            return;
        }

        event.setTarget(entity.getOwner().getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onGameBossSpawn(@NotNull EntitySpawnEvent event) {
        GameBoss boss = GameBossType.getGameBoss(event.getEntity());
        if (boss == null)
            return;

        GameBossSpawnEvent bwEvent = new GameBossSpawnEvent(boss);
        Bukkit.getPluginManager().callEvent(bwEvent);

        if (bwEvent.isCancelled())
            event.setCancelled(true);
    }

}
