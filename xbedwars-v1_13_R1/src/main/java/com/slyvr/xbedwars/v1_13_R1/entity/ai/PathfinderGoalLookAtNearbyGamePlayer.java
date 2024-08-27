package com.slyvr.xbedwars.v1_13_R1.entity.ai;

import com.slyvr.xbedwars.api.game.Game;
import net.minecraft.server.v1_13_R1.EntityInsentient;
import net.minecraft.server.v1_13_R1.EntityPlayer;
import net.minecraft.server.v1_13_R1.PathfinderGoalLookAtPlayer;
import org.bukkit.entity.Player;

public final class PathfinderGoalLookAtNearbyGamePlayer extends PathfinderGoalLookAtPlayer {

    private final Game game;

    public PathfinderGoalLookAtNearbyGamePlayer(EntityInsentient entity, Game game, float radius, float percentage) {
        super(entity, EntityPlayer.class, radius, percentage);

        this.game = game;
    }

    public PathfinderGoalLookAtNearbyGamePlayer(EntityInsentient entity, Game game, float radius) {
        super(entity, EntityPlayer.class, radius);

        this.game = game;
    }

    @Override
    public boolean a() {
        super.a();

        if (!(this.b instanceof EntityPlayer))
            return false;

        Player player = ((EntityPlayer) this.b).getBukkitEntity();
        return !game.isSpectator(player) && game.getGamePlayer(player) != null;
    }

}