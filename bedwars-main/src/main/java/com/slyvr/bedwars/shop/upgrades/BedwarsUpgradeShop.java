package com.slyvr.bedwars.shop.upgrades;

import com.cryptomorin.xseries.XMaterial;
import com.slyvr.bedwars.api.event.shop.ShopOpenEvent;
import com.slyvr.bedwars.api.game.GameMode;
import com.slyvr.bedwars.api.lang.Language;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.api.lang.NamesRegistry;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.api.shop.ShopCategory;
import com.slyvr.bedwars.api.shop.content.Purchasable;
import com.slyvr.bedwars.api.shop.content.data.PurchasableCost;
import com.slyvr.bedwars.api.shop.content.data.PurchasableDescription;
import com.slyvr.bedwars.api.shop.upgrades.UpgradeShop;
import com.slyvr.bedwars.api.shop.upgrades.type.PurchasableTrap;
import com.slyvr.bedwars.api.trap.Trap;
import com.slyvr.bedwars.configuration.shop.UpgradeShopConfig;
import com.slyvr.bedwars.shop.AbstractShopCategory;
import com.slyvr.bedwars.shop.ShopInventory;
import com.slyvr.bedwars.utils.ItemManager;
import com.slyvr.bedwars.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class BedwarsUpgradeShop extends AbstractShopCategory implements UpgradeShop {

    public static final BedwarsUpgradeShop EMPTY;

    private static final Map<Language, ItemStack> SEPARATORS = new HashMap<>();
    private static final Map<Language, ItemStack> EMPTY_TRAPS = new HashMap<>();
    private static final NamesRegistry NAMES;

    static {
        ItemStack separator = XMaterial.GRAY_STAINED_GLASS_PANE.parseItem();
        ItemStack empty = XMaterial.GRAY_STAINED_GLASS.parseItem();

        ItemFlag[] flags = ItemFlag.values();

        Map<Language, String> names = new HashMap<>();
        for (Language lang : Language.values()) {
            ItemManager separator_manager = new ItemManager(separator);
            separator_manager.setDisplayName(MessageUtils.formatLangMessage(Message.SHOP_DISPLAY_SEPARATOR_PURCHASABLE, lang));
            separator_manager.addLore(MessageUtils.formatLangMessage(Message.SHOP_DISPLAY_SEPARATOR_QUEUE, lang));
            separator_manager.addItemFlags(flags);

            ItemManager empty_manager = new ItemManager(empty);
            empty_manager.addItemFlags(flags);

            String description = MessageUtils.formatLangMessage(Message.SHOP_DISPLAY_TRAPS_EMPTY_DESCRIPTION, lang);
            for (String line : description.split("\\n"))
                empty_manager.addLore(ChatColor.GRAY + line);

            BedwarsUpgradeShop.SEPARATORS.put(lang, separator_manager.getItemStack());
            BedwarsUpgradeShop.EMPTY_TRAPS.put(lang, empty_manager.getItemStack());

            names.put(lang, MessageUtils.formatLangMessage(Message.SHOP_DISPLAY_UPGRADE_TITLE, lang));
        }

        NAMES = new NamesRegistry(names, "Upgrades & Traps");
        EMPTY = new BedwarsUpgradeShop(null, null);
    }

    private final Map<Integer, UpgradeShopCategory> categories;

    public BedwarsUpgradeShop(@Nullable Map<Integer, UpgradeShopCategory> categories, @Nullable Map<Integer, Purchasable> content) {
        super(NAMES, new ItemStack(Material.BEDROCK), content);

        if (categories == null) {
            this.categories = Collections.EMPTY_MAP;
            return;
        }

        Map<Integer, UpgradeShopCategory> categories_map = new HashMap<>();
        for (Entry<Integer, UpgradeShopCategory> entry : categories.entrySet()) {
            UpgradeShopCategory category = entry.getValue();
            if (category == null)
                throw new IllegalArgumentException("Shop's category cannot be null!");

            int slot = entry.getKey();
            if (slot < 0 || slot > 20)
                throw new IllegalArgumentException("Shop's category slot must be between 0 and 20!");

            categories_map.put(slot, category);
        }

        this.categories = Collections.unmodifiableMap(categories_map);
    }

    @Override
    public @NotNull Map<Integer, UpgradeShopCategory> getCategories() {
        return categories;
    }

    @Override
    public @Nullable UpgradeShopCategory getCategory(int index) {
        return categories.get(index);
    }

    @Override
    public @NotNull ShopType getType() {
        return ShopType.UPGRADES;
    }

    @Override
    public boolean populate(@NotNull Inventory inventory, @NotNull GamePlayer player) {
        if (inventory == null || player == null)
            return false;

        for (Entry<Integer, Purchasable> entry : content.entrySet())
            inventory.setItem(toInventorySlot(entry.getKey()), entry.getValue().getDisplayItem(player));

        return true;
    }

    @Override
    public boolean click(@NotNull GamePlayer player, @Range(from = 0, to = 53) int slot) {
        if (player == null)
            return false;

        int category_index = toCategorySlot(slot);
        if (category_index == -1)
            return false;

        ShopCategory category = categories.get(category_index);
        if (category != null)
            return open(player, category);

        Purchasable purchasable = content.get(category_index);
        return purchasable != null && purchasable.purchase(player);
    }

    @Override
    public boolean open(@NotNull GamePlayer player, @Nullable ShopCategory category) {
        if (player == null || (category instanceof UpgradeShopCategory && !categories.containsValue((UpgradeShopCategory) category)))
            return false;

        if (category == null)
            category = this;

        ShopOpenEvent event = new ShopOpenEvent(this, category, player);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return false;

        Language language = MessageUtils.getPlayerLanguage(player.getPlayer());

        ShopInventory shop_inv = new ShopInventory(this, category, category.getNames().getName(language));
        Inventory inv = shop_inv.getInventory();

        // Placing Purchasables
        Map<Trap, Object[]> traps_data = new HashMap<>();
        category.forEach((slot, purchasable) -> {
            ItemStack display = purchasable.getDisplayItem(player);
            inv.setItem(toInventorySlot(slot), display);

            if (!(purchasable instanceof PurchasableTrap))
                return;

            PurchasableTrap trap_item = (PurchasableTrap) purchasable;
            traps_data.put(trap_item.getTrap(), new Object[]{display, trap_item.getDescription()});
        });

        // Placing Categories
        if (category == this) {
            for (Entry<Integer, UpgradeShopCategory> entry : categories.entrySet())
                inv.setItem(toInventorySlot(entry.getKey()), entry.getValue().getDisplayItem(player));
        }

        // Placing Traps Section
        UpgradeShopConfig config = UpgradeShopConfig.getInstance();
        GameMode mode = player.getGame().getMode();

        List<Trap> team_traps = player.getTeam().getTrapManager().getTraps();
        for (int i = 0; i < config.getTrapsLimit(mode); i++) {
            if (i >= team_traps.size()) {
                inv.setItem(39 + i, getTrapsSectionEmptyIcon(language, config.getTrapCost(mode, i), team_traps.size()));
                continue;
            }

            Object[] data = traps_data.get(team_traps.get(i));
            if (data == null)
                continue;

            inv.setItem(39 + i, getTrapsSectionIcon(language, (ItemStack) data[0], (PurchasableDescription) data[1]));
        }

        // Placing Separators
        ItemStack SEPARATOR = SEPARATORS.get(language);
        for (int i = 27; i <= 35; i++)
            inv.setItem(i, SEPARATOR);

        return player.getPlayer().openInventory(inv) != null;
    }

    @Override
    public boolean open(@NotNull GamePlayer player) {
        return open(player, this);
    }

    @Override
    public boolean contains(@NotNull UpgradeShopCategory category) {
        return categories.containsValue(category);
    }

    @NotNull
    private ItemStack getTrapsSectionIcon(@NotNull Language lang, @NotNull ItemStack item, @NotNull PurchasableDescription desc) {
        ItemManager manager = new ItemManager(item);
        manager.setDisplayName(ChatColor.GREEN + manager.getDisplayName());
        manager.getLore().clear();

        String description = desc.getDescription(lang);
        for (String line : description.split("\\\\n"))
            manager.addLore(ChatColor.GRAY + line);

        return manager.getItemStack();
    }

    @NotNull
    private ItemStack getTrapsSectionEmptyIcon(@NotNull Language lang, @NotNull PurchasableCost cost, int index) {
        ItemManager manager = new ItemManager(EMPTY_TRAPS.get(lang));
        manager.setDisplayName(MessageUtils.formatLangMessage(Message.SHOP_DISPLAY_TRAPS_EMPTY, lang, index + 1));

        manager.addLore((String) null);
        manager.addLore(MessageUtils.formatLangMessage(Message.SHOP_DISPLAY_TRAPS_NEXT, lang, PurchasableCost.getColoredRepresentingText(cost, lang)));
        return manager.getItemStack();
    }

    public @Range(from = -1, to = 20) int toCategorySlot(@Range(from = 0, to = 53) int index) {
        if (index < 0 || index > 53)
            return -1;

        if (index >= 1 && index <= 7)
            return index - 1;

        if (index >= 10 && index <= 16)
            return index - 3;

        if (index >= 19 && index <= 25)
            return index - 5;

        return -1;
    }

    private @Range(from = -1, to = 53) int toInventorySlot(@Range(from = 0, to = 20) int index) {
        if (index >= 0 && index <= 6)
            return index + 1;

        if (index >= 7 && index <= 13)
            return index + 3;

        if (index >= 14 && index <= 20)
            return index + 5;

        return -1;
    }

}