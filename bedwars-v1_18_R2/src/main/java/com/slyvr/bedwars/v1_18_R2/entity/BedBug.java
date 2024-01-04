package com.slyvr.bedwars.v1_18_R2.entity;

import com.slyvr.bedwars.api.entity.GameEntity;
import com.slyvr.bedwars.api.entity.GameEntityType;
import com.slyvr.bedwars.api.entity.GameEntityType.SpawnType;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.v1_18_R2.entity.ai.NearestAttackableEnemyGoal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Silverfish;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BedBug extends Silverfish implements GameEntity {

    public static final GameEntityType<?> BED_BUG_TYPE = new GameEntityType<BedBug>("Bed Bug", SpawnType.PROJECTILE) {

        @Override
        public @NotNull BedBug create(@NotNull GamePlayer owner, @NotNull Location loc) {
            return new BedBug(owner, loc);
        }

        @Override
        public @Nullable BedBug getEntity(@NotNull Entity entity) {
            if (entity == null)
                return null;

            net.minecraft.world.entity.Entity nms_entity = ((CraftEntity) entity).getHandle();
            if (!(nms_entity instanceof BedBug game_entity))
                return null;

            return game_entity.owner != null ? game_entity : null;
        }
    };

    private final GamePlayer owner;

    public BedBug(@NotNull GamePlayer owner, @NotNull Location loc) {
        super(EntityType.SILVERFISH, ((CraftWorld) loc.getWorld()).getHandle());

        this.owner = owner;

        this.persist = true;
        this.dead = true;

        this.setPos(loc.getX(), loc.getY(), loc.getZ());
        this.setRot(loc.getYaw(), loc.getPitch());

        this.initPathfinders();
    }

    private void initPathfinders() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1F, true));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.25F, 16F));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1F));
        this.goalSelector.addGoal(4, new FloatGoal(this));

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
        return BED_BUG_TYPE;
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