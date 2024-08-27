package com.slyvr.xbedwars.trap;

import com.slyvr.xbedwars.api.event.player.trap.GamePlayerTrapTriggerEvent;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.api.team.TeamColor;
import com.slyvr.xbedwars.api.trap.TrapTarget;
import com.slyvr.xbedwars.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class AlarmTrap extends AbstractTrap {

    public AlarmTrap(int duration) {
        super("Alarm Trap", TrapTarget.ENEMY, duration);
    }

    public AlarmTrap() {
        this(10);
    }

    @Override
    public boolean onTrigger(@NotNull GamePlayer player, @NotNull TeamColor color) {
        if (player == null || color == null || player.isTrapSafe())
            return false;

        GamePlayerTrapTriggerEvent event = new GamePlayerTrapTriggerEvent(player, this, color);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return false;

        Player bukkit_player = player.getPlayer();
        bukkit_player.removePotionEffect(PotionEffectType.INVISIBILITY);

        Collection<GamePlayer> team_players = player.getGame().getTeamPlayers(color);
        if (team_players.isEmpty())
            return true;

        String message = ChatUtils.format("&cAlarm trap set off by &a" + bukkit_player.getDisplayName() + " &cfrom " + color.getColoredName() + " &cteam!");

        for (GamePlayer trap_owner : team_players) {
            Player trap_player = trap_owner.getPlayer();
            if (player.getGame().isDisconnected(trap_player))
                continue;

            trap_player.sendMessage(message);
        }

        return true;
    }

}
