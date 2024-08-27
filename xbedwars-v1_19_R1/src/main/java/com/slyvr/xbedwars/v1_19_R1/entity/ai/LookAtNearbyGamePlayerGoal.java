package com.slyvr.xbedwars.v1_19_R1.entity.ai;

import com.slyvr.xbedwars.api.game.Game;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import org.bukkit.entity.Player;

public final class LookAtNearbyGamePlayerGoal extends LookAtPlayerGoal {

    private final Game game;

    public LookAtNearbyGamePlayerGoal(PathfinderMob entity, Game game, float radius) {
        super(entity, ServerPlayer.class, radius, 1F);

        this.game = game;
    }

    @Override
    public boolean canUse() {
        if (!super.canUse())
            return false;

        Player player = ((ServerPlayer) lookAt).getBukkitEntity();
        return game.contains(player) && !game.isSpectator(player) && !game.isDisconnected(player);
    }

}