package com.slyvr.bedwars.v1_19_R2.entity.ai;

import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.api.team.TeamColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public final class NearestAttackableEnemyGoal extends NearestAttackableTargetGoal<ServerPlayer> {

    public NearestAttackableEnemyGoal(PathfinderMob mob, Game game, TeamColor color) {
        super(mob, ServerPlayer.class, 0, false, true, getPredicate(game, color));

    }

    private static @Nullable Predicate<LivingEntity> getPredicate(Game game, TeamColor color) {
        return entity -> {
            if (!(entity instanceof ServerPlayer))
                return false;

            Player player = ((ServerPlayer) entity).getBukkitEntity();
            if (game.isSpectator(player))
                return false;

            GamePlayer gp = game.getGamePlayer(player);
            return gp != null && gp.getTeamColor() != color;
        };
    }

}