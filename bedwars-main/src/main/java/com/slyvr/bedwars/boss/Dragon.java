package com.slyvr.bedwars.boss;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.boss.GameBoss;
import com.slyvr.bedwars.api.boss.GameBossType;
import com.slyvr.bedwars.api.team.GameTeam;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class Dragon implements GameBoss {

    private static final Map<Entity, Dragon> SPAWNED_BOSSES = new HashMap<>();

    public static final GameBossType<?> DRAGON_BOSS_TYPE = new GameBossType<Dragon>("Dragon") {
        @Override
        public @NotNull Dragon create(@NotNull GameTeam team, @NotNull Location loc) {
            return new Dragon(team, loc);
        }

        @Override
        public @Nullable Dragon getBoss(@NotNull Entity entity) {
            return SPAWNED_BOSSES.get(entity);
        }
    };

    private final GameTeam team;
    private final Location loc;

    private Entity spawned;

    public Dragon(@NotNull GameTeam team, @NotNull Location loc) {
        Preconditions.checkNotNull(team, "GameBoss' team cannot be null!");
        Preconditions.checkNotNull(loc, "GameBoss' spawn-point cannot be null!");

        this.team = team;
        this.loc = loc;
    }

    @Override
    public @NotNull GameTeam getGameTeam() {
        return team;
    }

    @Override
    public @NotNull GameBossType<?> getBossType() {
        return DRAGON_BOSS_TYPE;
    }

    @Override
    public @Nullable Entity getEntity() {
        return spawned;
    }

    @Override
    public @NotNull Entity spawn() {
        Dragon.SPAWNED_BOSSES.remove(spawned);
        return (spawned = loc.getWorld().spawnEntity(loc, EntityType.ENDER_DRAGON));
    }

    @Override
    public boolean remove() {
        if (spawned == null)
            return false;

        this.spawned.remove();
        return spawned.isDead();
    }

    @Override
    public boolean isDead() {
        return spawned != null && spawned.isDead();
    }

}
