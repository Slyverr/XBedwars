package com.slyvr.bedwars.v1_16_R3.entity;

import com.slyvr.bedwars.api.entity.GameEntity;
import com.slyvr.bedwars.api.entity.GameEntityType;
import com.slyvr.bedwars.api.entity.GameEntityType.SpawnType;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.v1_16_R3.entity.ai.PathfinderGoalLookAtNearbyGamePlayer;
import com.slyvr.bedwars.v1_16_R3.entity.ai.PathfinderGoalNearestAttackableEnemy;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BedBug extends EntitySilverfish implements GameEntity {

    public static final GameEntityType<?> BED_BUG_TYPE = new GameEntityType<BedBug>("Bed Bug", SpawnType.PROJECTILE) {

        @Override
        public @NotNull BedBug create(@NotNull GamePlayer owner, @NotNull Location loc) {
            return new BedBug(owner, loc);
        }

        @Override
        public @Nullable BedBug getEntity(@NotNull Entity entity) {
            if (entity == null)
                return null;

            net.minecraft.server.v1_16_R3.Entity nms_entity = ((CraftEntity) entity).getHandle();
            if (!(nms_entity instanceof BedBug))
                return null;

            BedBug game_entity = (BedBug) nms_entity;
            return game_entity.owner != null ? game_entity : null;
        }
    };

    private final GamePlayer owner;

    public BedBug(GamePlayer owner, Location loc) {
        super(EntityTypes.SILVERFISH, ((CraftWorld) loc.getWorld()).getHandle());

        this.owner = owner;

        this.persistent = true;
        this.dead = true;

        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        this.initPathfinders();
    }

    private void initPathfinders() {
        this.goalSelector.a(1, new PathfinderGoalMeleeAttack(this, 1.0D, false));
        this.goalSelector.a(2, new PathfinderGoalMoveTowardsTarget(this, 0.25D, 32F));
        this.goalSelector.a(3, new PathfinderGoalLookAtNearbyGamePlayer(this, owner.getGame(), 16F));
        this.goalSelector.a(4, new PathfinderGoalRandomStroll(this, 0.25D));
        this.goalSelector.a(5, new PathfinderGoalFloat(this));

        this.targetSelector.a(1, new PathfinderGoalNearestAttackableEnemy(this, owner.getGame(), owner.getTeamColor()));
    }

    @Override
    public void initPathfinder() {
        return;
    }

    @Override
    public void dropDeathLoot(DamageSource damagesource, int i, boolean flag) {
        return;
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
        return world.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM) ? getBukkitEntity() : null;
    }

    private void resetHealth() {
        this.setHealth(this.getMaxHealth());
        this.dead = false;
    }

    public boolean remove() {
        return !dead && (dead = true);
    }

    @Override
    public boolean isDead() {
        return dead;
    }

}