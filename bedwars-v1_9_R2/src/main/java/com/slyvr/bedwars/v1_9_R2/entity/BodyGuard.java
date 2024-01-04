package com.slyvr.bedwars.v1_9_R2.entity;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.entity.GameEntity;
import com.slyvr.bedwars.api.entity.GameEntityType;
import com.slyvr.bedwars.api.entity.GameEntityType.SpawnType;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.v1_9_R2.entity.ai.PathfinderGoalLookAtNearbyGamePlayer;
import com.slyvr.bedwars.v1_9_R2.entity.ai.PathfinderGoalNearestAttackableEnemy;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BodyGuard extends EntityIronGolem implements GameEntity {

    public static final GameEntityType<?> BODY_GUARD_TYPE = new GameEntityType<BodyGuard>("Body Guard", SpawnType.SPAWN_EGG) {

        @Override
        public @NotNull BodyGuard create(@NotNull GamePlayer owner, @NotNull Location loc) {
            return new BodyGuard(owner, loc);
        }

        @Override
        public @Nullable BodyGuard getEntity(@NotNull Entity entity) {
            if (entity == null)
                return null;

            net.minecraft.server.v1_9_R2.Entity nms_entity = ((CraftEntity) entity).getHandle();
            if (!(nms_entity instanceof BodyGuard))
                return null;

            BodyGuard game_entity = (BodyGuard) nms_entity;
            return game_entity.owner != null ? game_entity : null;
        }
    };

    private final GamePlayer owner;

    public BodyGuard(@NotNull GamePlayer owner, @NotNull Location loc) {
        super(((CraftWorld) Preconditions.checkNotNull(loc, "GameEntity's spawn point cannot be null!").getWorld()).getHandle());

        Preconditions.checkNotNull(owner, "GameEntity's owner cannot be null!");

        this.owner = owner;

        this.dead = true;
        this.persistent = true;

        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        this.initPathfinders();
    }

    public BodyGuard(@NotNull World world) {
        super(world);

        this.owner = null;
    }

    private void initPathfinders() {
        this.goalSelector = new PathfinderGoalSelector(world.methodProfiler);
        this.targetSelector = new PathfinderGoalSelector(world.methodProfiler);

        this.goalSelector.a(1, new PathfinderGoalMeleeAttack(this, 1.0D, false));
        this.goalSelector.a(2, new PathfinderGoalMoveTowardsTarget(this, 0.25D, 32F));
        this.goalSelector.a(3, new PathfinderGoalLookAtNearbyGamePlayer(this, owner.getGame(), 16F));
        this.goalSelector.a(4, new PathfinderGoalRandomStroll(this, 0.25D));
        this.goalSelector.a(5, new PathfinderGoalFloat(this));

        this.targetSelector.a(1, new PathfinderGoalNearestAttackableEnemy(this, owner.getGame(), owner.getTeamColor()));
    }

    @Override
    public void dropDeathLoot(boolean flag, int i) {
    }

    @Override
    public void dropEquipment(boolean flag, int i) {
    }

    @Override
    public int getExpReward() {
        return 0;
    }

    @Override
    public @NotNull GamePlayer getOwner() {
        return owner;
    }

    @Override
    public @NotNull GameEntityType<?> getGameEntityType() {
        return BODY_GUARD_TYPE;
    }

    @Override
    public @NotNull Entity getEntity() {
        return getBukkitEntity();
    }

    @Override
    public @NotNull Entity spawn() {
        if (!dead)
            return getBukkitEntity();

        this.resetHealth();
        return world.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM) ? getBukkitEntity() : null;
    }

    private void resetHealth() {
        this.setHealth(this.getMaxHealth());
        this.dead = false;
    }

    @Override
    public boolean remove() {
        return !dead && (dead = true);
    }

    @Override
    public boolean isDead() {
        return dead;
    }

}