package com.slyvr.xbedwars.settings;

import com.cryptomorin.xseries.XMaterial;
import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.api.entity.GameEntityType;
import com.slyvr.xbedwars.api.game.GameMode;
import com.slyvr.xbedwars.api.game.phase.GamePhase;
import com.slyvr.xbedwars.api.game.phase.GamePhasePreset;
import com.slyvr.xbedwars.api.npc.ShopNPCType;
import com.slyvr.xbedwars.api.reward.GameRewardReason;
import com.slyvr.xbedwars.api.reward.GameRewardType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BedwarsGameSettings {

    public static final ShopNPCType<?> VILLAGER = ShopNPCType.getByName("Villager");

    private static final Map<GameRewardType, Map<GameRewardReason, GameRewardStorage>> REWARDS_STORAGE = new HashMap<>();
    private static final Map<GameEntityType<?>, int[]> ENTITIES_SETTINGS = new HashMap<>();

    private static final Object[] fireball_settings = new Object[5];
    private static final Object[] sponge_settings = new Object[2];
    private static final Object[] bridge_settings = new Object[3];
    private static final Object[] tnt_settings = new Object[5];

    private static final int[] bed_bug_settings = new int[3];
    private static final int[] body_guard_settings = new int[3];

    private static int game_player_reconnect_respawn_time;
    private static int game_player_default_respawn_time;
    private static int game_reward_time;
    private static int game_shout_time;

    private static int magic_milk;

    private BedwarsGameSettings() {
    }


    public static int getDefaultRespawnTime() {
        return game_player_default_respawn_time;
    }


    public static int getReconnectRespawnTime() {
        return game_player_reconnect_respawn_time;
    }


    public static int getShoutingTime() {
        return game_shout_time;
    }


    public static int getTimeToPlayForReward() {
        return game_reward_time;
    }


    public static int getRewardAmount(@NotNull GameRewardType type, @NotNull GameRewardReason reason, @Nullable GameMode mode) {
        Map<GameRewardReason, GameRewardStorage> rewards_storage = REWARDS_STORAGE.get(type);
        if (rewards_storage == null)
            return 0;

        GameRewardStorage storage = rewards_storage.get(reason);
        return storage != null ? storage.getRewardAmount(mode) : 0;
    }

    public static boolean isFireballBouncing() {
        return (boolean) fireball_settings[0];
    }

    public static boolean isFireballIncendiary() {
        return (boolean) fireball_settings[1];
    }

    public static float getFireballExplosionPower() {
        return (float) fireball_settings[3];
    }

    public static float getFireballExplosionDamage() {
        return (float) fireball_settings[4];
    }

    public static float getFireballSpeed() {
        return (float) fireball_settings[2];
    }

    // TNT
    public static boolean showTNTExplosionTime() {
        return (boolean) tnt_settings[0];
    }

    public static boolean isTNTIncendiary() {
        return (boolean) tnt_settings[1];
    }

    public static float getTNTExplosionDamage() {
        return (float) tnt_settings[3];
    }

    public static float getTNTExplosionPower() {
        return (float) tnt_settings[4];
    }

    public static int getTNTExplosionTime() {
        return (int) tnt_settings[2];
    }

    // Bridge Egg
    public static XMaterial getBridgeBlockType() {
        return (XMaterial) bridge_settings[0];
    }

    public static int getBridgeLength() {
        return (int) bridge_settings[1];
    }

    public static int getBridgeWidth() {
        return (int) bridge_settings[2];
    }

    // Sponge
    public static int getSpongeParticlesRadius() {
        return (int) sponge_settings[0];
    }

    public static int getSpongeParticlesRate() {
        return (int) sponge_settings[1];
    }


    // Game Entities
    public static int getLifeDuration(@NotNull GameEntityType<?> type) {
        int[] data_array = ENTITIES_SETTINGS.get(type);
        return data_array != null ? data_array[0] : 0;
    }

    public static int getDamage(@NotNull GameEntityType<?> type) {
        int[] data_array = ENTITIES_SETTINGS.get(type);
        return data_array != null ? data_array[1] : 0;
    }

    public static int getHealth(@NotNull GameEntityType<?> type) {
        int[] data_array = ENTITIES_SETTINGS.get(type);
        return data_array != null ? data_array[2] : 0;
    }

    public static int getLimit(@NotNull GameEntityType<?> type) {
        int[] data_array = ENTITIES_SETTINGS.get(type);
        return data_array != null ? data_array[3] : -1;
    }

    public static void loadSettings() {
        FileConfiguration config = XBedwars.getInstance().getConfig();

        BedwarsGameSettings.loadGamePhases(config);
        BedwarsGameSettings.loadGameRewards(config);
        BedwarsGameSettings.loadGameMechanics(config);
        BedwarsGameSettings.loadGameEntities(config);

        BedwarsGameSettings.game_player_reconnect_respawn_time = config.getInt("Game-Settings.Game-Respawn.Reconnect");
        if (game_player_reconnect_respawn_time < 0)
            BedwarsGameSettings.game_player_reconnect_respawn_time = 10;

        BedwarsGameSettings.game_player_default_respawn_time = config.getInt("Game-Settings.Game-Respawn.Default");
        if (game_player_default_respawn_time < 0)
            BedwarsGameSettings.game_player_default_respawn_time = 5;

        BedwarsGameSettings.game_shout_time = Math.max(config.getInt("Game-Settings.Game-Shouting-Time"), 0);
    }

    @NotNull
    private static String invalidPhase(@NotNull String phase) {
        return XBedwars.PREFIX + ChatColor.RED + "Could not recognize phase " + ChatColor.YELLOW + '\'' + phase + '\'' + ChatColor.RED + '!';
    }

    // Game Phases
    private static void loadGamePhases(@NotNull FileConfiguration config) {
        ConfigurationSection presets_section = config.getConfigurationSection("Game-Settings.Game-Phases");
        if (presets_section == null) {
            GamePhasePreset.register(new GamePhasePreset("Default", XBedwars.getDefaultPhases()));
            return;
        }

        for (String preset_name : presets_section.getKeys(false)) {
            List<GamePhase> phases = new ArrayList<>();

            for (String phase_name : presets_section.getStringList(preset_name)) {
                GamePhase phase = GamePhase.getByName(phase_name);
                if (phase != null || phase == GamePhase.GAME_END) {
                    phases.add(phase);
                    continue;
                }

                XBedwars.getBedwarsLogger().info("Could not recognize phase '" + phase_name + "'!");
            }

            GamePhasePreset.register(new GamePhasePreset(preset_name, phases));
        }

    }

    // Game Rewards
    private static void loadGameRewards(@NotNull FileConfiguration config) {
        BedwarsGameSettings.game_reward_time = config.getInt("Game-Settings.Game-Rewards-Time");
        if (game_reward_time <= 0)
            BedwarsGameSettings.game_reward_time = 90;

        ConfigurationSection rewards_section = config.getConfigurationSection("Game-Settings.Game-Rewards");
        if (rewards_section == null)
            return;

        for (String reward_type : rewards_section.getKeys(false)) {
            GameRewardType type = GameRewardType.getByName(reward_type.replace('-', ' '));
            if (type == null)
                continue;

            ConfigurationSection reasons_section = rewards_section.getConfigurationSection(reward_type);
            if (reasons_section == null)
                continue;

            Map<GameRewardReason, GameRewardStorage> result = new HashMap<>();
            for (String reward_reason : reasons_section.getKeys(false)) {
                GameRewardReason reason = GameRewardReason.getByReason(reward_reason.replace('-', ' '));
                if (reason == null)
                    continue;

                ConfigurationSection reason_section = reasons_section.getConfigurationSection(reward_reason);
                if (reason_section == null) {
                    int default_value = reasons_section.getInt(reward_reason);
                    if (default_value > 0)
                        result.put(reason, new GameRewardStorage(null, default_value));

                    continue;
                }

                Map<GameMode, Integer> rewards = new HashMap<>();
                int default_amount = 0;

                for (String reason_section_key : reason_section.getKeys(false)) {
                    if (reason_section_key.equalsIgnoreCase("Default")) {
                        default_amount = reason_section.getInt(reason_section_key);
                        continue;
                    }

                    GameMode mode = GameMode.getByString(reason_section_key);
                    if (mode == null)
                        continue;

                    int amount = reason_section.getInt(reason_section_key);
                    if (amount <= 0)
                        continue;

                    rewards.put(mode, amount);
                }

                if (!rewards.isEmpty())
                    result.put(reason, new GameRewardStorage(rewards, Math.max(default_amount, 0)));
            }

            BedwarsGameSettings.REWARDS_STORAGE.put(type, result);
        }

    }

    // Game Mechanics
    private static void loadGameMechanics(@NotNull FileConfiguration config) {
        BedwarsGameSettings.loadTNTSettings(config);
        BedwarsGameSettings.loadFireballSettings(config);
        BedwarsGameSettings.loadBridgeEggSettings(config);
        BedwarsGameSettings.loadSpongeSettings(config);
    }

    private static void loadTNTSettings(@NotNull FileConfiguration config) {
        BedwarsGameSettings.tnt_settings[0] = config.getBoolean("Game-Settings.Game-Mechanics.TNT.Display-Explosion-Time");
        BedwarsGameSettings.tnt_settings[1] = config.getBoolean("Game-Settings.Game-Mechanics.TNT.Explosion-Fire");

        float explosion_damage = (float) config.getDouble("Game-Settings.Game-Mechanics.TNT.Explosion-Damage");
        if (explosion_damage <= 0)
            explosion_damage = 8F;

        float explosion_power = (float) config.getDouble("Game-Settings.Game-Mechanics.TNT.Explosion-Power");
        if (explosion_power <= 0)
            explosion_power = 4F;

        int explosion_time = config.getInt("Game-Settings.Game-Mechanics.TNT.Explosion-Time");
        if (explosion_time <= 0)
            explosion_time = 60;

        BedwarsGameSettings.tnt_settings[2] = explosion_time;
        BedwarsGameSettings.tnt_settings[3] = explosion_damage;
        BedwarsGameSettings.tnt_settings[4] = explosion_power;
    }

    private static void loadFireballSettings(@NotNull FileConfiguration config) {
        BedwarsGameSettings.fireball_settings[0] = config.getBoolean("Game-Settings.Game-Mechanics.Fireball.Fireball-Bounce");
        BedwarsGameSettings.fireball_settings[1] = config.getBoolean("Game-Settings.Game-Mechanics.Fireball.Explosion-Fire");

        float fireball_speed = (float) config.getDouble("Game-Settings.Game-Mechanics.Fireball.Fireball-Speed");
        if (fireball_speed <= 0)
            fireball_speed = 1.5F;

        float fireball_power = (float) config.getDouble("Game-Settings.Game-Mechanics.Fireball.Explosion-Power");
        if (fireball_power <= 0)
            fireball_power = 2F;

        float fireball_damage = (float) config.getDouble("Game-Settings.Game-Mechanics.Fireball.Explosion-Damage");
        if (fireball_damage <= 0)
            fireball_damage = 8F;

        BedwarsGameSettings.fireball_settings[2] = fireball_speed;
        BedwarsGameSettings.fireball_settings[3] = fireball_power;
        BedwarsGameSettings.fireball_settings[4] = fireball_damage;
    }

    private static void loadBridgeEggSettings(@NotNull FileConfiguration config) {
        Optional<XMaterial> optional = XMaterial.matchXMaterial(config.getString("Game-Settings.Game-Mechanics.Bridge-Egg.Bridge-Block-Type", ""));
        if (optional.isPresent()) {
            Material material = optional.get().parseMaterial();
            if (!material.isBlock()) {
                XBedwars.getBedwarsLogger().info("Bridge-Egg's block type cannot be an item!");
                optional = Optional.of(XMaterial.WHITE_WOOL);
            } else if (material == Material.BEDROCK) {
                XBedwars.getBedwarsLogger().info("BEDROCK AS BRIDGE BLOCK TYPE ??! Nah, You can't :D");
                optional = Optional.of(XMaterial.WHITE_WOOL);
            }

        }

        if (optional.isPresent() && optional.get() == XMaterial.BEDROCK) {
            optional = Optional.of(XMaterial.WHITE_WOOL);

            XBedwars.getBedwarsLogger().info("BEDROCK AS BRIDGE BLOCK ??! Nah, You can't :D");
        }

        int bridge_length = Math.max(config.getInt("Game-Settings.Game-Mechanics.Bridge-Egg.Bridge-Length"), 5);
        int bridge_width = Math.max(config.getInt("Game-Settings.Game-Mechanics.Bridge-Egg.Bridge-Width"), 1);

        BedwarsGameSettings.bridge_settings[0] = optional.orElse(XMaterial.WHITE_WOOL);
        BedwarsGameSettings.bridge_settings[1] = Math.min(bridge_length, 30);
        BedwarsGameSettings.bridge_settings[2] = Math.min(bridge_width, 2);
    }

    private static void loadSpongeSettings(@NotNull FileConfiguration config) {
        int sponge_particles_radius = Math.max(config.getInt("Game-Settings.Game-Mechanics.Sponge.Particles-Radius"), 1);
        int sponge_particles_rate = Math.max(config.getInt("Game-Settings.Game-Mechanics.Sponge.Particles-Rate"), 2);

        BedwarsGameSettings.sponge_settings[0] = Math.min(sponge_particles_radius, 5);
        BedwarsGameSettings.sponge_settings[1] = Math.min(sponge_particles_rate, 4);
    }

    // Game Entities
    private static void loadGameEntities(@NotNull FileConfiguration config) {
        ConfigurationSection entities_section = config.getConfigurationSection("Game-Settings.Game-Entities");
        if (entities_section == null)
            return;

        for (String entity_name : entities_section.getKeys(false)) {
            GameEntityType<?> type = GameEntityType.getByName(entity_name);
            if (type == null) {
                XBedwars.getBedwarsLogger().info("Could not recognize game-entity '" + entity_name + "'!");
                continue;
            }

            int duration = entities_section.getInt(entity_name + ".Duration");
            int damage = entities_section.getInt(entity_name + ".Damage");
            int health = entities_section.getInt(entity_name + ".Health");
            int limit = entities_section.getInt(entity_name + ".Limit", -1);

            if (duration > 0 && health > 0)
                BedwarsGameSettings.ENTITIES_SETTINGS.put(type, new int[]{duration, damage, health, limit});
        }

    }

    private static final class GameRewardStorage {

        private final Map<GameMode, Integer> rewards_amount;
        private final int default_amount;

        public GameRewardStorage(@Nullable Map<GameMode, Integer> amounts, int amount) {
            this.rewards_amount = amounts;
            this.default_amount = amount;
        }

        public int getRewardAmount(@NotNull GameMode mode) {
            return rewards_amount != null ? rewards_amount.getOrDefault(mode, default_amount) : default_amount;
        }

    }

}