package com.slyvr.bedwars.trap;

import com.slyvr.bedwars.api.event.player.trap.GamePlayerTrapTriggerEvent;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.api.team.TeamColor;
import com.slyvr.bedwars.api.trap.TrapTarget;
import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public final class MiningFatigueTrap extends AbstractTrap {

    private static final PotionEffect SLOW_DIGGING = new PotionEffect(PotionEffectType.SLOW_DIGGING, 20 * 10, 0);

    public MiningFatigueTrap(int duration) {
        super("Mining Fatigue Trap", TrapTarget.ENEMY, duration);
    }

    public MiningFatigueTrap() {
        this(10);
    }

    @Override
    public boolean onTrigger(@NotNull GamePlayer player, @NotNull TeamColor color) {
        if (player == null || color == null || player.isTrapSafe())
            return false;

        GamePlayerTrapTriggerEvent event = new GamePlayerTrapTriggerEvent(player, this, color);
        Bukkit.getPluginManager().callEvent(event);

        return !event.isCancelled() && player.getPlayer().addPotionEffect(SLOW_DIGGING);
    }

}