package com.slyvr.xbedwars.configuration.shop;

import com.cryptomorin.xseries.XMaterial;
import com.slyvr.xbedwars.api.game.GameMode;
import com.slyvr.xbedwars.api.generator.Resource;
import com.slyvr.xbedwars.api.lang.NamesRegistry;
import com.slyvr.xbedwars.api.shop.content.Purchasable;
import com.slyvr.xbedwars.api.shop.content.TieredPurchasable.PurchasableTier;
import com.slyvr.xbedwars.api.shop.content.data.PurchasableCost;
import com.slyvr.xbedwars.api.shop.content.data.PurchasableCosts;
import com.slyvr.xbedwars.api.shop.content.data.PurchasableDescription;
import com.slyvr.xbedwars.api.shop.upgrades.UpgradeShop;
import com.slyvr.xbedwars.api.shop.upgrades.UpgradeShop.UpgradeShopCategory;
import com.slyvr.xbedwars.api.trap.Trap;
import com.slyvr.xbedwars.api.upgrade.TieredUpgrade;
import com.slyvr.xbedwars.api.upgrade.Upgrade;
import com.slyvr.xbedwars.configuration.Configuration;
import com.slyvr.xbedwars.manager.BedwarsTrapsManager;
import com.slyvr.xbedwars.manager.BedwarsUpgradesManager;
import com.slyvr.xbedwars.shop.upgrades.BedwarsUpgradeShop;
import com.slyvr.xbedwars.shop.upgrades.BedwarsUpgradeShopCategory;
import com.slyvr.xbedwars.shop.upgrades.type.PurchasableForge;
import com.slyvr.xbedwars.shop.upgrades.type.PurchasableTrap;
import com.slyvr.xbedwars.shop.upgrades.type.PurchasableUpgrade;
import com.slyvr.xbedwars.shop.upgrades.type.TieredPurchasableUpgrade;
import com.slyvr.xbedwars.upgrade.custom.tiered.ForgeUpgrade;
import com.slyvr.xbedwars.utils.ConfigurationUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class UpgradeShopConfig extends Configuration {

    private static final UpgradeShopConfig INSTANCE = new UpgradeShopConfig();

    private final Map<GameMode, UpgradeShopData> shops = new HashMap<>();

    private final Map<String, UpgradeShopCategoryData> shop_categories = new HashMap<>();
    private final Map<String, Purchasable> shop_purchasables = new HashMap<>();

    private List<PurchasableCost> default_costs;

    private UpgradeShopConfig() {
        super("Shops/UpgradeShop.yml");

        this.loadPurchasables();
        this.loadCategories();
        this.loadShops();
        this.loadCosts();
    }

    @Nullable
    public UpgradeShop getShop(@NotNull GameMode mode) {
        UpgradeShopData storage = getShopData(mode);
        return storage != null ? storage.shop : null;
    }

    @NotNull
    public PurchasableCost getTrapCost(@Nullable GameMode mode, int count) {
        if (mode == null || count < 0)
            return PurchasableCost.FREE;

        UpgradeShopData storage = getShopData(mode);
        return storage.getTrapCost(count);
    }

    public int getTrapsLimit(@NotNull GameMode mode) {
        UpgradeShopData storage = getShopData(mode);
        return storage.costs.size();
    }

    @Nullable
    private Purchasable getPurchasable(@NotNull String name) {
        return shop_purchasables.get(name);
    }

    @NotNull
    private UpgradeShopData getShopData(@NotNull GameMode mode) {
        return mode != null ? shops.computeIfAbsent(mode, key -> new UpgradeShopData(mode)) : null;
    }

    @Nullable
    private Purchasable getPurchasable(@NotNull ConfigurationSection section) {
        PurchasableType type = PurchasableType.getByName(section.getString("purchasable-type"));
        if (type == null)
            return null;

        switch (type) {
            case TIERED:
                return getPurchasableTieredUpgrade(section);
            case UPGRADE:
                return getPurchasableUpgrade(section);
            case TRAP:
                return getPurchasableTrap(section);
            default:
                return null;
        }

    }

    @Nullable
    private Purchasable getPurchasableTieredUpgrade(@NotNull ConfigurationSection section) {
        ConfigurationSection tiers_section = section.getConfigurationSection("purchasable-tiers");
        if (tiers_section == null)
            return null;

        TieredUpgrade upgrade = BedwarsUpgradesManager.getInstance().getTieredUpgrade(section.getString("purchasable-upgrade"));
        if (upgrade == null)
            return null;

        ItemStack icon = getDisplayIcon(section);
        if (icon == null)
            return null;

        List<PurchasableTier> tiers = new ArrayList<>();
        for (String tier_name : tiers_section.getKeys(false)) {
            ConfigurationSection tier_section = tiers_section.getConfigurationSection(tier_name);
            if (tier_section == null)
                continue;

            PurchasableCosts costs = ConfigurationUtils.getPurchasableCosts(tier_section);
            NamesRegistry names = ConfigurationUtils.getPurchasableNames(tier_section);

            tiers.add(new PurchasableTier(names, costs));
        }

        if (tiers.isEmpty())
            return null;

        NamesRegistry names = ConfigurationUtils.getPurchasableNames(section);
        if (upgrade != ForgeUpgrade.FORGE_UPGRADE)
            return new TieredPurchasableUpgrade(names, icon, upgrade, tiers, ConfigurationUtils.getPurchasableDescription(section));
        else
            return new PurchasableForge(names, icon, tiers, ConfigurationUtils.getPurchasableDescription(section));
    }

    @Nullable
    private Purchasable getPurchasableUpgrade(@NotNull ConfigurationSection section) {
        Upgrade upgrade = BedwarsUpgradesManager.getInstance().getUpgrade(section.getString("purchasable-upgrade"));
        if (upgrade == null)
            return null;

        ItemStack icon = getDisplayIcon(section);
        if (icon == null)
            return null;

        PurchasableCosts costs = ConfigurationUtils.getPurchasableCosts(section);
        PurchasableDescription desc = ConfigurationUtils.getPurchasableDescription(section);

        NamesRegistry names = ConfigurationUtils.getPurchasableNames(section);
        return new PurchasableUpgrade(names, icon, upgrade, costs, desc);
    }

    @Nullable
    private Purchasable getPurchasableTrap(@NotNull ConfigurationSection section) {
        Trap trap = BedwarsTrapsManager.getInstance().getTrap(section.getString("purchasable-trap"));
        if (trap == null)
            return null;

        ItemStack icon = getDisplayIcon(section);
        if (icon == null)
            return null;

        NamesRegistry names = ConfigurationUtils.getPurchasableNames(section);
        return new PurchasableTrap(names, icon, trap, ConfigurationUtils.getPurchasableDescription(section));
    }

    @Nullable
    private ItemStack getDisplayIcon(@NotNull ConfigurationSection section) {
        String display_type = section.getString("purchasable-icon");
        if (display_type == null || display_type.isEmpty())
            return null;

        XMaterial material = XMaterial.matchXMaterial(display_type).orElse(null);
        return material != null ? material.parseItem() : null;
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
            if (purchasable != null)
                this.shop_purchasables.put(purchasable_name, purchasable);
        }

    }

    private void loadCategories() {
        ConfigurationSection categories_section = config.getConfigurationSection("Shop-Categories.Display-Item");
        if (categories_section == null)
            return;

        for (String category_name : categories_section.getKeys(false)) {
            ConfigurationSection category_section = categories_section.getConfigurationSection(category_name);
            if (category_section != null)
                this.shop_categories.put(category_name.toLowerCase(), new UpgradeShopCategoryData(category_section));
        }

    }

    private void loadShops() {
        for (GameMode mode : GameMode.values())
            this.getShop(mode);
    }

    private void loadCosts() {
        ConfigurationSection costs_section = config.getConfigurationSection("Shop-Settings.Traps Costs");
        if (costs_section == null) {
            this.default_costs = getCosts(config.getStringList("Shop-Settings.Traps Costs"));
            return;
        }

        for (String costs_section_key : costs_section.getKeys(false)) {
            if (!costs_section_key.equalsIgnoreCase("Default"))
                continue;

            this.default_costs = getCosts(costs_section.getStringList(costs_section_key));
            return;
        }

        List<PurchasableCost> costs = new ArrayList<>(3);
        costs.add(new PurchasableCost(Resource.DIAMOND, 1));
        costs.add(new PurchasableCost(Resource.DIAMOND, 2));
        costs.add(new PurchasableCost(Resource.DIAMOND, 4));

        this.default_costs = costs;
    }

    @NotNull
    private List<PurchasableCost> getCosts(@NotNull List<String> costs) {
        List<PurchasableCost> result = new ArrayList<>(3);

        int size = costs.size();
        for (int i = 0; i < 3; i++)
            result.add(i < size ? PurchasableCost.deserialize(costs.get(i), PurchasableCost.FREE) : PurchasableCost.FREE);

        return result;
    }

    @NotNull
    public static UpgradeShopConfig getInstance() {
        return INSTANCE;
    }

    private enum PurchasableType {

        UPGRADE,
        TIERED,
        TRAP;

        private static final Map<String, PurchasableType> BY_NAME = new HashMap<>(3, 1F);

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


    private final class UpgradeShopCategoryData {

        private final Map<GameMode, UpgradeShopCategory> categories = new HashMap<>();

        private final NamesRegistry names;
        private final ItemStack icon;
        private final String name;

        public UpgradeShopCategoryData(@NotNull ConfigurationSection section) {
            this.names = ConfigurationUtils.getCategoryNames(section);
            this.icon = ConfigurationUtils.getCategoryIcon(section);

            this.name = section.getName();
        }

        @NotNull
        public UpgradeShopCategory getCategory(@NotNull GameMode mode) {
            UpgradeShopCategory existing = categories.get(mode);
            if (existing != null)
                return existing;

            ConfigurationSection category_section = config.getConfigurationSection("Shop-Content." + name);
            if (category_section == null)
                return categories.computeIfAbsent(mode, key -> new BedwarsUpgradeShopCategory(names, icon));

            Map<Integer, Purchasable> purchasables = new HashMap<>();
            for (String category_section_key : category_section.getKeys(false)) {
                int slot = NumberConversions.toInt(category_section_key.toLowerCase().replace("slot-", ""));
                if (slot < 1 || slot > 21)
                    continue;

                ConfigurationSection slot_section = category_section.getConfigurationSection(category_section_key);
                if (slot_section == null) {
                    purchasables.put(slot - 1, shop_purchasables.get(category_section.getString(category_section_key)));
                    continue;
                }

                for (String slot_section_key : slot_section.getKeys(false)) {
                    if (!slot_section_key.equalsIgnoreCase(mode.getName()))
                        continue;

                    purchasables.put(slot - 1, shop_purchasables.get(slot_section.getString(slot_section_key)));
                    break;
                }

            }

            return categories.computeIfAbsent(mode, key -> new BedwarsUpgradeShopCategory(names, icon, purchasables));
        }

    }


    private final class UpgradeShopData {

        private final List<PurchasableCost> costs;
        private final UpgradeShop shop;

        public UpgradeShopData(@NotNull GameMode mode) {
            this.costs = loadTrapsCosts(mode);
            this.shop = loadUpgradeShop(mode);
        }

        @NotNull
        private UpgradeShop loadUpgradeShop(@NotNull GameMode mode) {
            ConfigurationSection slots_section = config.getConfigurationSection("Shop-Content");
            if (slots_section == null)
                return BedwarsUpgradeShop.EMPTY;

            Map<Integer, Purchasable> purchasables = new HashMap<>();
            Map<Integer, UpgradeShopCategory> categories = new HashMap<>();

            for (String slots_section_key : slots_section.getKeys(false)) {
                int slot = NumberConversions.toInt(slots_section_key.toLowerCase().replace("slot-", ""));
                if (slot < 1 || slot > 21)
                    continue;

                ConfigurationSection slot_section = slots_section.getConfigurationSection(slots_section_key);
                if (slot_section == null) {
                    this.setDisplayable(categories, purchasables, mode, slots_section.getString(slots_section_key), slot);
                    continue;
                }

                for (String mode_name : slot_section.getKeys(false)) {
                    if (!mode_name.equalsIgnoreCase(mode.getName()))
                        continue;

                    this.setDisplayable(categories, purchasables, mode, slot_section.getString(mode_name), slot);
                    break;
                }
            }

            return new BedwarsUpgradeShop(categories, purchasables);
        }

        @NotNull
        private List<PurchasableCost> loadTrapsCosts(@NotNull GameMode mode) {
            ConfigurationSection costs_section = config.getConfigurationSection("Shop-Settings.Traps Costs");
            if (costs_section == null)
                return getCosts(config.getStringList("Shop-Settings.Traps Costs"));

            for (String price_section_key : costs_section.getKeys(false)) {
                if (price_section_key.equalsIgnoreCase(mode.getName()))
                    return getCosts(costs_section.getStringList(price_section_key));
            }

            return default_costs;
        }

        @NotNull
        private PurchasableCost getTrapCost(int count) {
            return count >= costs.size() ? PurchasableCost.FREE : costs.get(count);
        }

        private void setDisplayable(@NotNull Map<Integer, UpgradeShopCategory> categories, @NotNull Map<Integer, Purchasable> content, @NotNull GameMode mode, @Nullable String name, int slot) {
            if (name == null)
                return;

            UpgradeShopCategoryData storage = shop_categories.get(name.toLowerCase());
            if (storage != null) {
                categories.put(slot - 1, storage.getCategory(mode));
                return;
            }

            Purchasable purchasable = shop_purchasables.get(name);
            if (purchasable != null) {
                content.put(slot - 1, purchasable);
            }

        }

    }

}