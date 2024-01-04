package com.slyvr.bedwars.generator;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.bedwars.api.event.generator.ResourceGeneratorStartEvent;
import com.slyvr.bedwars.api.event.generator.ResourceGeneratorStopEvent;
import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.generator.Resource;
import com.slyvr.bedwars.api.generator.team.TeamResourceGenerator;
import com.slyvr.bedwars.api.generator.team.TeamResourceGeneratorDrop;
import com.slyvr.bedwars.api.team.TeamColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;


public final class BedwarsTeamResourceGenerator extends AbstractResourceGenerator implements TeamResourceGenerator {

    private static final MetadataValue EMPTY_METADATA = new FixedMetadataValue(Bedwars.getInstance(), null);
    private static final Vector FLYING_RAIJIN_VECTOR = new Vector(0, 0.25, 0);

    private final Map<Resource, GeneratorDataStorage> drops;
    private final TeamColor color;
    private boolean isRunning;

    public BedwarsTeamResourceGenerator(@NotNull Game game, @NotNull Location loc, @NotNull TeamColor color, @NotNull Set<TeamResourceGeneratorDrop> drops) {
        super(game, loc);

        Preconditions.checkNotNull(color, "ResourceGenerator's team color cannot be null!");
        Preconditions.checkNotNull(drops, "ResourceGenerator's drops set cannot be null!");
        Preconditions.checkArgument(!drops.isEmpty(), "ResourceGenerator's drops set cannot be empty!");

        this.color = color;
        this.drops = new HashMap<>(drops.size());

        for (TeamResourceGeneratorDrop drop : drops) {
            if (drop == null)
                throw new IllegalArgumentException("ResourceGenerator's drop cannot be null!");

            this.drops.put(drop.getResource(), new GeneratorDataStorage(drop.clone()));
        }

    }

    @Override
    public @NotNull TeamColor getTeamColor() {
        return color;
    }

    @Override
    public @NotNull Set<TeamResourceGeneratorDrop> getDrops() {
        Set<TeamResourceGeneratorDrop> result = new HashSet<>(drops.size());

        for (GeneratorDataStorage data : drops.values())
            result.add(data.drop);

        return result;
    }

    @Override
    public @Nullable TeamResourceGeneratorDrop getDrop(@NotNull Resource resource) {
        if (resource == null)
            return null;

        GeneratorDataStorage data = drops.get(resource);
        return data != null ? data.drop : null;
    }

    @Override
    public @Nullable TeamResourceGeneratorDrop removeDrop(@NotNull Resource resource) {
        if (resource == null)
            return null;

        GeneratorDataStorage data = drops.remove(resource);
        return data != null ? data.drop : null;
    }

    @Override
    public void addDrop(@NotNull TeamResourceGeneratorDrop drop) {
        if (drop == null || drops.containsKey(drop.getResource()))
            return;

        GeneratorDataStorage data = new GeneratorDataStorage(drop);
        data.start();

        this.drops.put(drop.getResource(), data);
    }

    @Override
    public void updateDrop(@NotNull Resource resource) {
        GeneratorDataStorage data = drops.get(resource);
        if (data == null)
            return;

        data.stop();
        data.start();
    }

    @Override
    public void start() {
        if (isRunning)
            return;

        ResourceGeneratorStartEvent event = new ResourceGeneratorStartEvent(this);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        for (GeneratorDataStorage storage : drops.values())
            storage.start();

        this.isRunning = true;
    }

    @Override
    public void stop() {
        if (!isRunning)
            return;

        for (GeneratorDataStorage data : drops.values())
            data.stop();

        this.isRunning = false;
        Bukkit.getPluginManager().callEvent(new ResourceGeneratorStopEvent(this));
    }

    @Override
    public void setDropLocation(@NotNull Location loc) {
        if (loc == null)
            return;

        this.loc = loc.clone();
        if (!isRunning)
            return;

        for (Entry<Resource, GeneratorDataStorage> entry : drops.entrySet()) {
            GeneratorDataStorage data = entry.getValue();
            data.stop();
            data.start();
        }

    }

    @Override
    public boolean hasDrop(@NotNull Resource resource) {
        return resource != null && drops.containsKey(resource);
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    private final class GeneratorDataStorage {

        private final List<Item> dropped_items;

        private final TeamResourceGeneratorDrop drop;
        private final ItemStack item;

        private BukkitTask[] tasks;

        public GeneratorDataStorage(@NotNull TeamResourceGeneratorDrop drop) {

            this.drop = drop;
            this.item = new ItemStack(drop.getResource().getMaterial());

            this.dropped_items = new ArrayList<>();
        }

        public void stop() {
            for (BukkitTask task : tasks)
                task.cancel();

            this.tasks = null;
            this.clear();
        }

        private void start() {
            if (tasks != null)
                return;

            int dpm = drop.getDropsPerMinute();
            if (dpm <= 60) {
                tasks = new BukkitTask[]{startGenerator(loc, 1200 / dpm)};
                return;
            }

            this.tasks = new BukkitTask[(int) Math.ceil(dpm / 60.0)];

            for (int i = 0; i < tasks.length; i++) {
                if (dpm < 60) {
                    this.tasks[i] = startGenerator(loc, 1200 / dpm);
                    break;
                }

                this.tasks[i] = startGenerator(loc, 1200 / 60);
                dpm -= 60;
            }

        }

        @NotNull
        private BukkitTask startGenerator(@NotNull Location loc, long ticks) {
            return new BukkitRunnable() {

                private final World world = loc.getWorld();

                @Override
                public void run() {
                    int amount_found = 0;

                    for (Entity entity : world.getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
                        if (entity.getType() != EntityType.DROPPED_ITEM)
                            continue;

                        Item entity_item = (Item) entity;
                        if (!entity_item.hasMetadata("bedwars-drop"))
                            continue;

                        ItemStack dropped = entity_item.getItemStack();
                        if (dropped.getType() != item.getType())
                            continue;

                        amount_found += dropped.getAmount();
                        if (amount_found >= drop.getDropLimit())
                            return;
                    }

                    GeneratorDataStorage.this.drop();
                }

            }.runTaskTimer(Bedwars.getInstance(), 0, ticks);
        }

        private void drop() {
            Item drop = loc.getWorld().dropItem(loc, item);
            drop.setVelocity(FLYING_RAIJIN_VECTOR);

            if (this.drop.canSplit())
                drop.setMetadata("bedwars-drop", EMPTY_METADATA);

            this.dropped_items.add(drop);
        }

        private void clear() {
            for (int i = 0; i < dropped_items.size(); i++) {
                Item dropped = dropped_items.get(i);
                if (dropped == null)
                    continue;

                dropped.remove();
                dropped_items.set(i, null);
            }
        }

    }

}