package com.slyvr.xbedwars.trap;

import com.slyvr.xbedwars.api.event.player.trap.GamePlayerTrapTriggerEvent;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.api.team.TeamColor;
import com.slyvr.xbedwars.api.trap.TrapTarget;
import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public final class BlindnessTrap extends AbstractTrap {

    private static final PotionEffect BLINDNESS = new PotionEffect(PotionEffectType.BLINDNESS, 20 * 8, 0);

    public BlindnessTrap(int duration) {
        super("Blindness Trap", TrapTarget.ENEMY, duration);
    }

    public BlindnessTrap() {
        this(10);
    }

    @Override
    public boolean onTrigger(@NotNull GamePlayer player, @NotNull TeamColor color) {
        if (player == null || color == null || player.isTrapSafe())
            return false;

        GamePlayerTrapTriggerEvent event = new GamePlayerTrapTriggerEvent(player, this, color);
        Bukkit.getPluginManager().callEvent(event);

        return !event.isCancelled() && player.getPlayer().addPotionEffect(BLINDNESS);
    }

}
