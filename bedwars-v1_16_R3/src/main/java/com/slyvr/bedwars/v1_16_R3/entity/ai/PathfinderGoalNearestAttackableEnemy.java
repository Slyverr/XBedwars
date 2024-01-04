package com.slyvr.bedwars.v1_16_R3.entity.ai;

import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.api.team.TeamColor;
import net.minecraft.server.v1_16_R3.EntityCreature;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.PathfinderGoalNearestAttackableTarget;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

public final class PathfinderGoalNearestAttackableEnemy extends PathfinderGoalNearestAttackableTarget<EntityPlayer> {

    public PathfinderGoalNearestAttackableEnemy(EntityCreature mob, Game game, TeamColor color) {
        super(mob, EntityPlayer.class, 0, false, true, getPredicate(game, color));

    }

    private static Predicate<EntityLiving> getPredicate(Game game, TeamColor color) {
        return entity -> {
            if (!(entity instanceof EntityPlayer))
                return false;

            Player player = ((EntityPlayer) entity).getBukkitEntity();

            if (game.isSpectator(player))
                return false;

            GamePlayer gp = game.getGamePlayer(player);
            return gp != null && gp.getTeamColor() != color;
        };
    }

}