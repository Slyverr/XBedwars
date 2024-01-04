package com.slyvr.bedwars.v1_19_R1.entity;

import com.slyvr.bedwars.api.entity.GameEntity;
import com.slyvr.bedwars.api.entity.GameEntityType;
import com.slyvr.bedwars.api.entity.GameEntityType.SpawnType;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.v1_19_R1.entity.ai.LookAtNearbyGamePlayerGoal;
import com.slyvr.bedwars.v1_19_R1.entity.ai.NearestAttackableEnemyGoal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.IronGolem;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BodyGuard extends IronGolem implements GameEntity {

    public static final GameEntityType<?> BODY_GUARD_TYPE = new GameEntityType<BodyGuard>("Body Guard", SpawnType.SPAWN_EGG) {

        @Override
        public @NotNull BodyGuard create(@NotNull GamePlayer owner, @NotNull Location loc) {
            return new BodyGuard(owner, loc);
        }

        @Override
        public @Nullable BodyGuard getEntity(@NotNull Entity entity) {
            if (entity == null)
                return null;

            net.minecraft.world.entity.Entity nms_entity = ((CraftEntity) entity).getHandle();
            if (!(nms_entity instanceof BodyGuard game_entity))
                return null;

            return game_entity.owner != null ? game_entity : null;
        }
    };

    private final GamePlayer owner;

    public BodyGuard(@NotNull GamePlayer owner, @NotNull Location loc) {
        super(EntityType.IRON_GOLEM, ((CraftWorld) loc.getWorld()).getHandle());

        this.owner = owner;

        this.persist = true;
        this.dead = true;

        this.setPos(loc.getX(), loc.getY(), loc.getZ());
        this.setRot(loc.getYaw(), loc.getPitch());

        this.initPathfinders();
    }

    private void initPathfinders() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1F, true));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 1F, 16F));
        this.goalSelector.addGoal(3, new LookAtNearbyGamePlayerGoal(this, owner.getGame(), 8F));
        this.goalSelector.addGoal(4, new FloatGoal(this));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.6F));

        this.targetSelector.addGoal(1, new NearestAttackableEnemyGoal(this, owner.getGame(), owner.getTeamColor()));
    }

    @Override
    public void registerGoals() {
    }

    @Override
    public void dropAllDeathLoot(DamageSource damagesource) {
    }

    @Override
    public void checkDespawn() {
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
        return !(dead = !level.addFreshEntity(this, SpawnReason.CUSTOM)) ? getBukkitEntity() : null;
    }

    private void resetHealth() {
        this.setHealth(this.getMaxHealth());
        this.dead = false;
    }

    public boolean remove() {
        if (dead)
            return false;

        this.remove(RemovalReason.DISCARDED);
        return dead = true;
    }

    @Override
    public boolean isDead() {
        return dead;
    }

}