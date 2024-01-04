package com.slyvr.bedwars.listener.npc;

import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.npc.ShopNPC;
import com.slyvr.bedwars.api.npc.ShopNPCType;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.configuration.shop.ItemShopConfig;
import com.slyvr.bedwars.configuration.shop.UpgradeShopConfig;
import com.slyvr.bedwars.game.BedwarsGame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jetbrains.annotations.NotNull;


public final class ShopNPCListener implements Listener {

    @EventHandler
    public void onGamePlayerShopNPCDamage(@NotNull EntityDamageByEntityEvent event) {
        ShopNPC npc = ShopNPCType.getShopNPC(event.getEntity());
        if (npc == null)
            return;

        if (event.getDamager() instanceof Player)
            this.openNPCShop((Player) event.getDamager(), npc);

        event.setCancelled(true);
    }

    @EventHandler
    public void onGamePlayerShopNPCClick(@NotNull PlayerInteractEntityEvent event) {
        ShopNPC npc = ShopNPCType.getShopNPC(event.getRightClicked());
        if (npc == null)
            return;

        event.setCancelled(true);
        this.openNPCShop(event.getPlayer(), npc);
    }

    private void openNPCShop(@NotNull Player player, @NotNull ShopNPC npc) {
        Game game = BedwarsGame.getPlayerGame(player);
        if (game == null || !game.isRunning() || game.isSpectator(player))
            return;

        GamePlayer game_player = game.getGamePlayer(player);
        switch (npc.getShopType()) {
            case UPGRADES:
                UpgradeShopConfig.getInstance().getShop(game.getMode()).open(game_player);
                break;
            case ITEMS:
                ItemShopConfig.getInstance().getShop(game.getMode()).open(game_player);
                break;
        }
    }

}