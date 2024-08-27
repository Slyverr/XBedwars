package com.slyvr.xbedwars.configuration.shop;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.slyvr.xbedwars.api.entity.GameEntityType;
import com.slyvr.xbedwars.api.game.GameMode;
import com.slyvr.xbedwars.api.lang.NamesRegistry;
import com.slyvr.xbedwars.api.player.ArmorType;
import com.slyvr.xbedwars.api.shop.content.Purchasable;
import com.slyvr.xbedwars.api.shop.content.TieredPurchasable.PurchasableTier;
import com.slyvr.xbedwars.api.shop.content.data.PurchasableCosts;
import com.slyvr.xbedwars.api.shop.content.data.PurchasableDescription;
import com.slyvr.xbedwars.api.shop.items.ItemShop;
import com.slyvr.xbedwars.api.shop.items.ItemShop.ItemShopCategory;
import com.slyvr.xbedwars.api.shop.items.type.TieredPurchasableItem.TieredItemStack;
import com.slyvr.xbedwars.configuration.Configuration;
import com.slyvr.xbedwars.shop.items.BedwarsItemShop;
import com.slyvr.xbedwars.shop.items.BedwarsItemShopCategory;
import com.slyvr.xbedwars.shop.items.type.PurchasableArmor;
import com.slyvr.xbedwars.shop.items.type.PurchasableItemStack;
import com.slyvr.xbedwars.shop.items.type.PurchasablePotion;
import com.slyvr.xbedwars.shop.items.type.TieredPurchasableItemStack;
import com.slyvr.xbedwars.utils.ConfigurationUtils;
import com.slyvr.xbedwars.utils.ItemManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class ItemShopConfig extends Configuration {

    private static final ItemShopConfig INSTANCE = new ItemShopConfig();

    private final Map<GameMode, ItemShopData> shops = new HashMap<>();
    private final Map<String, ItemShopCategoryData> shop_categories = new HashMap<>();

    private final Map<String, Purchasable> path_purchasable = new HashMap<>();
    private final Map<Purchasable, String> purchasable_path = new HashMap<>();

    private final List<String> default_categories = new ArrayList<>();

    private ItemShopConfig() {
        super("Shops/ItemShop.yml");

        this.loadPurchasables();
        this.loadCategories();
        this.loadShops();
    }

    @Nullable
    public ItemShop getShop(@NotNull GameMode mode) {
        return mode != null ? shops.computeIfAbsent(mode, key -> new ItemShopData(mode)).getShop() : null;
    }

    @Nullable
    private Purchasable getPurchasable(@NotNull ConfigurationSection section) {
        PurchasableType type = PurchasableType.getByName(section.getString("purchasable-type"));
        if (type == null)
            return null;

        switch (type) {
            case ITEM:
                return getPurchasableItem(section);
            case ARMOR:
                return getPurchasableArmor(section);
            case POTION:
                return getPurchasablePotion(section);
            case ENTITY:
                return getPurchasableEntity(section);
            case TIERED:
                return getPurchasableTieredItem(section);
            default:
                return null;
        }

    }

    @Nullable
    private Purchasable getPurchasableItem(@NotNull ConfigurationSection section) {
        ItemStack item = getItemStack(section);
        if (item == null)
            return null;

        PurchasableCosts costs = ConfigurationUtils.getPurchasableCosts(section);
        PurchasableDescription desc = ConfigurationUtils.getPurchasableDescription(section);

        NamesRegistry names = ConfigurationUtils.getPurchasableNames(section);
        return new PurchasableItemStack(names, item, costs, desc, section.getBoolean("purchasable-permanent"));
    }

    @Nullable
    private Purchasable getPurchasableArmor(@NotNull ConfigurationSection section) {
        ArmorType type = ArmorType.getByName(section.getString("purchasable-armor"));
        if (type == null)
            return null;

        PurchasableCosts costs = ConfigurationUtils.getPurchasableCosts(section);
        PurchasableDescription desc = ConfigurationUtils.getPurchasableDescription(section);

        NamesRegistry names = ConfigurationUtils.getPurchasableNames(section);
        return new PurchasableArmor(names, type, costs, desc);
    }

    @Nullable
    private Purchasable getPurchasablePotion(@NotNull ConfigurationSection section) {
        PotionEffectType type = PotionEffectType.getByName(section.getString("purchasable-effect", ""));
        if (type == null)
            return null;

        int level = section.getInt("purchasable-level");
        if (level <= 0)
            return null;

        int duration = section.getInt("purchasable-duration");
        if (duration <= 0)
            return null;

        boolean particles = section.getBoolean("purchasable-particles");

        PurchasableCosts costs = ConfigurationUtils.getPurchasableCosts(section);
        PurchasableDescription desc = ConfigurationUtils.getPurchasableDescription(section);

        NamesRegistry names = ConfigurationUtils.getPurchasableNames(section);
        return new PurchasablePotion(names, new PotionEffect(type, duration * 20, level - 1, false, particles), costs, desc);
    }

    @Nullable
    private Purchasable getPurchasableEntity(@NotNull ConfigurationSection section) {
        GameEntityType<?> type = GameEntityType.getByName(section.getString("purchasable-entity"));
        if (type == null)
            return null;

        PurchasableCosts costs = ConfigurationUtils.getPurchasableCosts(section);
        PurchasableDescription desc = ConfigurationUtils.getPurchasableDescription(section);

        NamesRegistry names = ConfigurationUtils.getPurchasableNames(section);
        return new PurchasableItemStack(names, type.getSpawnItem(), costs, desc, false);
    }

    @Nullable
    private Purchasable getPurchasableTieredItem(@NotNull ConfigurationSection section) {
        ConfigurationSection tiers_section = section.getConfigurationSection("purchasable-tiers");
        if (tiers_section == null)
            return null;

        List<PurchasableTier> tiers = new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();

        for (String tier_name : tiers_section.getKeys(false)) {
            ConfigurationSection tier_section = tiers_section.getConfigurationSection(tier_name);
            if (tier_section == null)
                continue;

            ItemStack item = getItemStack(tier_section);
            if (item == null)
                continue;

            PurchasableCosts costs = ConfigurationUtils.getPurchasableCosts(tier_section);
            NamesRegistry names = ConfigurationUtils.getPurchasableNames(tier_section);

            items.add(item);
            tiers.add(new PurchasableTier(names, costs));
        }

        if (tiers.isEmpty() || items.isEmpty())
            return null;

        NamesRegistry names = ConfigurationUtils.getPurchasableNames(section);
        return new TieredPurchasableItemStack(names, new TieredItemStack(items), tiers, ConfigurationUtils.getPurchasableDescription(section));
    }

    @Nullable
    private ItemStack getItemStack(@NotNull ConfigurationSection section) {
        try {
            ItemManager manager = new ItemManager(XMaterial.matchXMaterial(section.getString("purchasable-item")).orElse(null).parseItem());

            String display_name = section.getString("purchasable-name");
            if (display_name != null && !display_name.trim().isEmpty())
                manager.setDisplayName(ChatColor.RESET + display_name);

            int amount = section.getInt("purchasable-amount");
            if (amount > 1)
                manager.setAmount(amount);

            for (String enchantment : section.getStringList("purchasable-enchantments"))
                this.enchant(manager, enchantment);

            manager.setUnbreakable(section.getBoolean("purchasable-unbreakable"));
            return manager.getItemStack();
        } catch (Exception ignore) {
            return null;
        }

    }

    private void enchant(@NotNull ItemManager manager, @NotNull String enchantment) {
        String[] data = enchantment.split(":");
        if (data.length < 2)
            return;

        XEnchantment ench = XEnchantment.matchXEnchantment(data[0]).orElse(null);
        if (ench == null)
            return;

        int level = NumberConversions.toInt(data[1]);
        if (level >= 1)
            manager.addEnchantment(ench.getEnchant(), level, true);
    }

    private void loadPurchasables() {
        ConfigurationSection purchasables_section = config.getConfigurationSection("Shop-Purchasables");
        if (purchasables_section == null)
            return;

        for (String purchasable_name : purchasables_section.getKeys(false)) {
            ConfigurationSection purchasable_section = purchasables_section.getConfigurationSection(purchasable_name);
            if (purchasable_section == null)
                continue;

            Purchasable purchasable = getPurchasable(purchasable_section);
            if (purchasable == null)
                continue;

            this.path_purchasable.put(purchasable_name, purchasable);
            this.purchasable_path.put(purchasable, purchasable_name);
        }

    }

    private void loadDefaultCategories() {
        ConfigurationSection categories_section = config.getConfigurationSection("Shop-Categories.Categories-Order");
        if (categories_section == null) {
            this.default_categories.addAll(config.getStringList("Shop-Categories.Categories-Order"));
            return;
        }

        for (String categories_section_key : categories_section.getKeys(false)) {
            if (!categories_section_key.equalsIgnoreCase("Default"))
                continue;

            this.default_categories.addAll(categories_section.getStringList(categories_section_key));
            break;
        }

    }

    private void loadCategories() {
        this.loadDefaultCategories();

        ConfigurationSection categories_section = config.getConfigurationSection("Shop-Categories.Categories");
        if (categories_section == null)
            return;

        for (String category_name : categories_section.getKeys(false)) {
            ConfigurationSection category_section = categories_section.getConfigurationSection(category_name);
            if (category_section != null)
                this.shop_categories.put(category_name.toLowerCase(), new ItemShopCategoryData(category_section));
        }

    }

    private void loadShops() {
        for (GameMode mode : GameMode.values())
            this.getShop(mode);
    }

    @Nullable
    public static String getPurchasablePath(@NotNull Purchasable purchasable) {
        return INSTANCE.purchasable_path.get(purchasable);
    }

    @Nullable
    public static Purchasable getPathPurchasable(@NotNull String path) {
        return INSTANCE.path_purchasable.get(path);
    }

    @NotNull
    public static ItemShopConfig getInstance() {
        return INSTANCE;
    }


    private enum PurchasableType {

        ITEM,
        ARMOR,
        POTION,
        ENTITY,
        TIERED;

        private static final Map<String, PurchasableType> BY_NAME = new HashMap<>(5, 1F);

        static {
            for (PurchasableType type : values())
                PurchasableType.BY_NAME.put(type.name().toLowerCase(), type);
        }

        PurchasableType() {
        }

        @Nullable
        public static PurchasableType getByName(@NotNull String name) {
            return name != null ? BY_NAME.get(name.toLowerCase()) : null;
        }

    }


    private final class ItemShopCategoryData {

        private final Map<GameMode, ItemShopCategory> categories = new HashMap<>();

        private final NamesRegistry names;
        private final ItemStack icon;
        private final String name;

        public ItemShopCategoryData(@NotNull ConfigurationSection section) {
            this.names = ConfigurationUtils.getCategoryNames(section);
            this.icon = ConfigurationUtils.getCategoryIcon(section);

            this.name = section.getName();
        }

        @NotNull
        public ItemShopCategory getCategory(@NotNull GameMode mode) {
            ItemShopCategory existing = categories.get(mode);
            if (existing != null)
                return existing;

            ConfigurationSection category_section = config.getConfigurationSection("Shop-Content." + name);
            if (category_section == null)
                return categories.computeIfAbsent(mode, key -> new BedwarsItemShopCategory(names, icon));

            Map<Integer, Purchasable> purchasables = new HashMap<>();
            for (String category_section_key : category_section.getKeys(false)) {
                int slot = NumberConversions.toInt(category_section_key.toLowerCase().replace("slot-", ""));
                if (slot < 1 || slot > 21)
                    continue;

                ConfigurationSection slot_section = category_section.getConfigurationSection(category_section_key);
                if (slot_section == null) {
                    purchasables.put(slot - 1, path_purchasable.get(category_section.getString(category_section_key)));
                    continue;
                }

                for (String slot_section_key : slot_section.getKeys(false)) {
                    if (slot_section_key.equalsIgnoreCase(mode.getName()))
                        purchasables.put(slot - 1, path_purchasable.get(category_section.getString(category_section_key)));
                }

            }

            return categories.computeIfAbsent(mode, key -> new BedwarsItemShopCategory(names, icon, purchasables));
        }

    }


    private final class ItemShopData {

        private final ItemShop shop;

        public ItemShopData(@NotNull GameMode mode) {
            List<String> categories_names = getShopCategoriesNames(mode);

            List<ItemShopCategory> categories = new ArrayList<>(9);
            for (String category_name : categories_names) {
                ItemShopCategoryData storage = shop_categories.get(category_name.toLowerCase());
                if (storage != null)
                    categories.add(storage.getCategory(mode));
            }

            this.shop = new BedwarsItemShop(categories);
        }

        @NotNull
        public ItemShop getShop() {
            return shop;
        }

        @NotNull
        private List<String> getShopCategoriesNames(@NotNull GameMode mode) {
            ConfigurationSection categories_section = config.getConfigurationSection("Shop-Categories.Categories-Order");
            if (categories_section == null)
                return default_categories;

            for (String mode_name : categories_section.getKeys(false)) {
                if (mode_name.equalsIgnoreCase(mode.getName()))
                    return categories_section.getStringList(mode_name);
            }

            return default_categories;
        }

    }

}