package com.slyvr.xbedwars.v1_9_R2.npc;

import com.google.common.base.Preconditions;
import com.slyvr.xbedwars.api.npc.ShopNPC;
import com.slyvr.xbedwars.api.npc.ShopNPCType;
import com.slyvr.xbedwars.api.team.GameTeam;
import com.slyvr.xbedwars.v1_9_R2.entity.ai.PathfinderGoalLookAtNearbyGamePlayer;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityVillager;
import net.minecraft.server.v1_9_R2.PathfinderGoalSelector;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.slyvr.xbedwars.api.shop.Shop.ShopType;

public final class Villager extends EntityVillager implements ShopNPC {

    public static final ShopNPCType<?> VILLAGER_NPC_TYPE = new ShopNPCType<ShopNPC>("Villager") {

        @Override
        public @NotNull Villager create(@NotNull GameTeam team, @NotNull ShopType type, @NotNull Location loc) {
            return new Villager(team, type, loc);
        }

        @Override
        public @Nullable Villager getNPC(@NotNull org.bukkit.entity.Entity entity) {
            if (entity == null)
                return null;

            net.minecraft.server.v1_9_R2.Entity nms_entity = ((CraftEntity) entity).getHandle();
            if (!(nms_entity instanceof ShopNPC))
                return null;

            Villager shop_npc = (Villager) nms_entity;
            return shop_npc.team != null && shop_npc.type != null ? shop_npc : null;
        }
    };

    private final GameTeam team;
    private final ShopType type;

    public Villager(@NotNull GameTeam team, @NotNull ShopType type, @NotNull Location loc) {
        super(((CraftWorld) Preconditions.checkNotNull(loc, "ShopNPC's spawn-point cannot be null!").getWorld()).getHandle());

        Preconditions.checkNotNull(team, "ShopNPC's team cannot be null!");
        Preconditions.checkNotNull(type, "ShopNPC's shop-type cannot be null!");

        this.team = team;
        this.type = type;

        this.dead = true;
        this.persistent = true;

        this.c(true);

        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        this.initPathfinders();
    }

    public Villager(@NotNull World world) {
        super(world);

        this.team = null;
        this.type = null;
    }

    private void initPathfinders() {
        this.goalSelector = new PathfinderGoalSelector(world.methodProfiler);
        this.targetSelector = new PathfinderGoalSelector(world.methodProfiler);

        this.goalSelector.a(1, new PathfinderGoalLookAtNearbyGamePlayer(this, team.getGame(), 32F, 1F));
    }

    @Override
    public void collide(Entity entity) {
        return;
    }

    @Override
    public void move(double d0, double d1, double d2) {
        return;
    }

    @Override
    public void g(double d0, double d1, double d2) {
        return;
    }

    @Override
    public @NotNull GameTeam getGameTeam() {
        return team;
    }

    @Override
    public @NotNull ShopNPCType<?> getNPCType() {
        return VILLAGER_NPC_TYPE;
    }

    @Override
    public @NotNull ShopType getShopType() {
        return type;
    }

    @Override
    public boolean spawn() {
        if (!dead)
            return false;

        this.dead = false;
        return world.addEntity(this, SpawnReason.CUSTOM);
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