package com.slyvr.xbedwars.v1_19_R2.npc;

import com.slyvr.xbedwars.api.npc.ShopNPC;
import com.slyvr.xbedwars.api.npc.ShopNPCType;
import com.slyvr.xbedwars.api.team.GameTeam;
import com.slyvr.xbedwars.v1_19_R2.entity.ai.LookAtNearbyGamePlayerGoal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.slyvr.xbedwars.api.shop.Shop.ShopType;

public final class Villager extends net.minecraft.world.entity.npc.Villager implements ShopNPC {

    public static final ShopNPCType<?> VILLAGER_NPC_TYPE = new ShopNPCType<Villager>("Villager") {

        @Override
        public @NotNull Villager create(@NotNull GameTeam team, @NotNull ShopType type, @NotNull Location loc) {
            return new Villager(team, type, loc);
        }

        @Override
        public @Nullable Villager getNPC(@NotNull org.bukkit.entity.Entity entity) {
            if (entity == null)
                return null;

            Entity nms_entity = ((CraftEntity) entity).getHandle();
            if (!(nms_entity instanceof Villager shop_npc))
                return null;

            return shop_npc.team != null && shop_npc.type != null ? shop_npc : null;
        }
    };

    private final GameTeam team;
    private final ShopType type;

    public Villager(GameTeam team, ShopType type, Location loc) {
        super(EntityType.VILLAGER, ((CraftWorld) loc.getWorld()).getHandle());

        this.team = team;
        this.type = type;

        this.persist = true;
        this.dead = true;

        this.setPos(loc.getX(), loc.getY(), loc.getZ());
        this.setRot(loc.getYaw(), loc.getPitch());

        this.setSilent(true);
        this.initPathfinders();
    }

    private void initPathfinders() {
        this.goalSelector.addGoal(1, new LookAtNearbyGamePlayerGoal(this, team.getGame(), 32F));
    }

    @Override
    public void registerGoals() {
        return;
    }

    @Override
    public void checkDespawn() {
        return;
    }

    @Override
    public void move(MoverType type, Vec3 vec3d) {
        return;
    }

    @Override
    public void push(Entity entity) {
        return;
    }

    @Override
    public void push(double d0, double d1, double d2) {
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
        return !(dead = !level.addFreshEntity(this, SpawnReason.CUSTOM));
    }

    @Override
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