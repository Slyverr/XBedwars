package com.slyvr.bedwars.generator;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.bedwars.api.event.generator.ResourceGeneratorStartEvent;
import com.slyvr.bedwars.api.event.generator.ResourceGeneratorStopEvent;
import com.slyvr.bedwars.api.event.player.connection.GamePlayerDisconnectEvent;
import com.slyvr.bedwars.api.event.player.connection.GamePlayerReconnectEvent;
import com.slyvr.bedwars.api.event.user.UserLanguageChangeEvent;
import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.generator.Resource;
import com.slyvr.bedwars.api.generator.tiered.ResourceGeneratorTier;
import com.slyvr.bedwars.api.generator.tiered.TieredResourceGenerator;
import com.slyvr.bedwars.api.generator.tiered.TieredResourceGeneratorPreset;
import com.slyvr.bedwars.api.lang.Language;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.api.user.User;
import com.slyvr.bedwars.manager.BedwarsUsersManager;
import com.slyvr.bedwars.utils.MessageUtils;
import com.slyvr.hologram.VirtualHologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;


public final class BedwarsTieredResourceGenerator extends AbstractResourceGenerator implements TieredResourceGenerator {

    private static final MetadataValue EMPTY_METADATA = new FixedMetadataValue(Bedwars.getInstance(), null);
    private static final Vector FLYING_RAIJIN_VECTOR = new Vector(0, 0.25, 0);

    private final TieredResourceGeneratorAnimation animation;
    private final TieredResourceGeneratorHologram hologram;
    private final TieredResourceGeneratorManager manager;
    private final TieredResourceGeneratorPreset preset;
    private final Resource resource;

    private boolean isRunning;

    public BedwarsTieredResourceGenerator(@NotNull Game game, @NotNull Location loc, @NotNull Resource resource, @NotNull TieredResourceGeneratorPreset preset) {
        super(game, loc);

        Preconditions.checkNotNull(resource, "Generator's resource cannot be null!");
        Preconditions.checkNotNull(preset, "Generator's preset cannot be null!");

        this.resource = resource;
        this.preset = preset;

        this.manager = new TieredResourceGeneratorManager();
        this.hologram = new TieredResourceGeneratorHologram();
        this.animation = new TieredResourceGeneratorAnimation();
    }

    @Override
    public @NotNull List<ResourceGeneratorTier> getTiers() {
        return preset.getTiers();
    }

    @Override
    public @Nullable ResourceGeneratorTier getCurrentTier() {
        return manager.current;
    }

    @Override
    public void setCurrentTier(int tier) {
        if (tier == 0) {
            this.stop();
            return;
        }

        if (tier == manager.tier || tier < 0 || tier > preset.size())
            return;

        this.manager.upgrade(tier);
        this.hologram.upgrade();
    }

    @Override
    public @NotNull Resource getDrop() {
        return resource;
    }

    @Override
    public void setDropLocation(@NotNull Location loc) {
        if (loc == null)
            return;

        this.loc = loc.clone();
        this.hologram.teleport(loc);
    }

    @Override
    public void start() {
        if (isRunning)
            return;

        ResourceGeneratorStartEvent event = new ResourceGeneratorStartEvent(this);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        this.manager.start();
        this.hologram.start();
        this.animation.start();

        this.isRunning = true;
    }

    @Override
    public void stop() {
        if (!isRunning)
            return;

        this.manager.stop();
        this.hologram.stop();
        this.animation.stop();

        this.isRunning = false;

        Bukkit.getPluginManager().callEvent(new ResourceGeneratorStopEvent(this));
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public int size() {
        return preset.size();
    }

    private final class TieredResourceGeneratorAnimation {

        private final ArmorStand animation_stand;
        private BukkitTask animation_task;

        public TieredResourceGeneratorAnimation() {
            ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().add(0, 1.35, 0), EntityType.ARMOR_STAND);
            stand.setHelmet(new ItemStack(resource.getBlock()));
            stand.setGravity(false);
            stand.setVisible(false);

            this.animation_stand = stand;
        }

        public void start() {
            this.animation_task = new BukkitRunnable() {

                private final Location loc = animation_stand.getLocation();

                private boolean moving_up = true;
                private float y_offset = 0.00F;
                private float speed = 0.00F;

                @Override
                public void run() {
                    if (moving_up)
                        this.moveUp();
                    else
                        this.moveDown();
                }

                private void moveUp() {
                    if (y_offset >= 0.25) {
                        this.moving_up = false;
                        this.speed = 0F;
                    }

                    this.y_offset += 0.0125F;
                    this.speed += 0.0125F;

                    animation_stand.teleport(loc.add(0, 0.0125, 0));
                    animation_stand.setHeadPose(animation_stand.getHeadPose().add(0, speed, 0));
                }

                private void moveDown() {
                    if (y_offset <= -0.25) {
                        this.moving_up = true;
                        this.speed = 0F;
                    }

                    this.y_offset -= 0.0125F;
                    this.speed += 0.0125F;

                    animation_stand.teleport(loc.subtract(0, 0.0125, 0));
                    animation_stand.setHeadPose(animation_stand.getHeadPose().subtract(0, speed, 0));
                }

            }.runTaskTimerAsynchronously(Bedwars.getInstance(), 0, 1L);

        }

        public void stop() {
            if (animation_task != null)
                this.animation_task.cancel();

            if (animation_stand != null)
                this.animation_stand.remove();
        }

    }

    private final class TieredResourceGeneratorHologram implements Listener {

        private final Map<Language, VirtualHologram> holograms = new HashMap<>();

        private final AtomicInteger holograms_time = new AtomicInteger();

        private BukkitTask holograms_task;

        public TieredResourceGeneratorHologram() {
            Bukkit.getPluginManager().registerEvents(this, Bedwars.getInstance());
        }

        @EventHandler
        public void onGamePlayerDisconnect(GamePlayerDisconnectEvent event) {
            if (!game.equals(event.getGamePlayer().getGame()))
                return;

            Player player = event.getGamePlayer().getPlayer();
            this.remove(MessageUtils.getPlayerLanguage(player), player);
        }

        @EventHandler
        public void onGamePlayerReconnect(GamePlayerReconnectEvent event) {
            if (!game.equals(event.getGamePlayer().getGame()))
                return;

            Player player = event.getGamePlayer().getPlayer();
            this.add(MessageUtils.getPlayerLanguage(player), player);
        }

        @EventHandler
        public void onUserLanguageChange(UserLanguageChangeEvent event) {
            User user = event.getUser();
            if (!user.isOnline() || !game.equals(user.getGame()))
                return;

            Player player = user.getPlayer().getPlayer();

            this.remove(event.getPreviousLanguage(), player);
            this.add(event.getCurrentLanguage(), player);
        }

        public void start() {
            Location clone = loc.clone().add(0, 2.5, 0);

            for (GamePlayer player : game.getGamePlayers()) {
                Language language = BedwarsUsersManager.getPlayerLanguage(player.getPlayer());

                VirtualHologram hologram = holograms.get(language);
                if (hologram != null) {
                    hologram.addViewer(player.getPlayer());
                    continue;
                }

                VirtualHologram created = new VirtualHologram(clone);
                created.addText(Message.GAME_RESOURCE_GENERATOR_WAITING.format(language, manager.current.getDropTime()));
                created.addText(manager.current.getNames().getName(language));
                created.addText(resource.getColoredName());

                created.addViewer(player.getPlayer());
                holograms.put(language, created);
            }

            for (VirtualHologram hologram : holograms.values())
                hologram.update();

            this.holograms_time.set(manager.current.getDropTime());

            this.holograms_task = new BukkitRunnable() {

                @Override
                public void run() {
                    if (holograms_time.get() == 0)
                        holograms_time.set(manager.current.getDropTime());

                    for (Entry<Language, VirtualHologram> entry : holograms.entrySet()) {
                        VirtualHologram hologram = entry.getValue();
                        hologram.setText(0, Message.GAME_RESOURCE_GENERATOR_WAITING.format(entry.getKey(), holograms_time.getAndDecrement()));
                        hologram.update();
                    }

                }

            }.runTaskTimerAsynchronously(Bedwars.getInstance(), 0L, 20L);
        }

        public void stop() {
            if (holograms_task != null)
                this.holograms_task.cancel();

            for (VirtualHologram hologram : holograms.values())
                hologram.remove();

            this.holograms.clear();

            GamePlayerDisconnectEvent.getHandlerList().unregister(this);
            GamePlayerReconnectEvent.getHandlerList().unregister(this);
            UserLanguageChangeEvent.getHandlerList().unregister(this);
        }

        public void upgrade() {
            if (manager.current == null)
                return;

            for (Entry<Language, VirtualHologram> entry : holograms.entrySet())
                entry.getValue().setText(1, manager.current.getNames().getName(entry.getKey()));

            this.holograms_time.set(manager.current.getDropTime());
        }

        public void teleport(@NotNull Location loc) {
            for (VirtualHologram hologram : holograms.values())
                hologram.teleport(loc);
        }

        private void remove(@NotNull Language lang, @NotNull Player player) {
            VirtualHologram hologram = holograms.get(lang);
            if (hologram == null)
                return;

            hologram.removeViewer(player);
            hologram.update();
        }

        private void add(@NotNull Language lang, @NotNull Player player) {
            VirtualHologram hologram = holograms.get(lang);
            if (hologram == null)
                return;

            hologram.addViewer(player);
            hologram.update();
        }

    }

    private final class TieredResourceGeneratorManager {

        private final List<Item> drops = new ArrayList<>();

        private final ItemStack item;

        private ResourceGeneratorTier current;
        private BukkitTask generator_task;
        private int tier = 1;

        public TieredResourceGeneratorManager() {
            this.item = new ItemStack(resource.getMaterial());
        }

        public void start() {
            this.current = preset.getTier(tier - 1);

            this.generator_task = new BukkitRunnable() {

                @Override
                public void run() {
                    int amount_found = 0;

                    for (Entity entity : loc.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
                        if (entity.getType() != EntityType.DROPPED_ITEM)
                            continue;

                        Item entity_item = (Item) entity;
                        if (!entity_item.hasMetadata("bedwars-tiered-drop"))
                            continue;

                        ItemStack dropped = entity_item.getItemStack();
                        if (dropped.getType() != resource.getMaterial())
                            continue;

                        amount_found += dropped.getAmount();
                        if (amount_found >= current.getDropLimit())
                            return;
                    }

                    TieredResourceGeneratorManager.this.drop();
                }
            }.runTaskTimer(Bedwars.getInstance(), 0, current.getDropTime() * 20L);

        }

        public void stop() {
            if (generator_task != null)
                this.generator_task.cancel();

            this.clear();
        }

        public void upgrade(int tier) {
            if (generator_task != null)
                this.generator_task.cancel();

            this.tier = tier;
            this.start();
        }

        private void drop() {
            Item drop = loc.getWorld().dropItem(loc, item);
            drop.setMetadata("bedwars-tiered-drop", EMPTY_METADATA);
            drop.setVelocity(FLYING_RAIJIN_VECTOR);

            this.drops.add(drop);
        }

        private void clear() {
            for (Item drop : drops)
                drop.remove();

            this.drops.clear();
        }

    }

}