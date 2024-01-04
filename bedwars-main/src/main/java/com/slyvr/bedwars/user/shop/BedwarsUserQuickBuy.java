package com.slyvr.bedwars.user.shop;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.event.user.shop.UserQuickBuyEditEvent;
import com.slyvr.bedwars.api.event.user.shop.UserQuickBuyEditEvent.QuickBuyAction;
import com.slyvr.bedwars.api.lang.Language;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.api.lang.NamesRegistry;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.api.shop.content.Purchasable;
import com.slyvr.bedwars.api.user.shop.UserQuickBuy;
import com.slyvr.bedwars.utils.ItemManager;
import com.slyvr.bedwars.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class BedwarsUserQuickBuy implements UserQuickBuy {

    private static final Map<Language, ItemStack> QUICK_BUY_ICONS = new HashMap<>();
    private static final Map<Language, ItemStack> EMPTY_ICONS = new HashMap<>();

    private static final NamesRegistry QUICK_BUY_NAMES;

    static {
        Map<Language, String> names = new HashMap<>();

        for (Language lang : Language.values()) {
            String category_name = MessageUtils.formatLangMessage(Message.SHOP_DISPLAY_QUICK_BUY_TITLE, lang);
            String empty_name = MessageUtils.formatLangMessage(Message.SHOP_DISPLAY_QUICK_BUY_EMPTY_SLOT_TITLE, lang);
            String empty_desc = MessageUtils.formatLangMessage(Message.SHOP_DISPLAY_QUICK_BUY_EMPTY_SLOT_DESCRIPTION, lang);

            ItemManager category_icon = new ItemManager(XMaterial.NETHER_STAR.parseItem());
            category_icon.setDisplayName(ChatColor.AQUA + category_name);

            ItemManager empty_icon = new ItemManager(XMaterial.RED_STAINED_GLASS_PANE.parseItem());
            empty_icon.setDisplayName(ChatColor.RED + empty_name);

            for (String line : empty_desc.split("\\n"))
                empty_icon.addLore(ChatColor.GRAY + line);

            names.put(lang, category_name);

            BedwarsUserQuickBuy.EMPTY_ICONS.put(lang, empty_icon.getItemStack());
            BedwarsUserQuickBuy.QUICK_BUY_ICONS.put(lang, category_icon.getItemStack());
        }

        QUICK_BUY_NAMES = new NamesRegistry(names, "Quick Buy");
    }

    private final Map<Integer, Purchasable> content;

    private final OfflinePlayer owner;

    public BedwarsUserQuickBuy(@NotNull OfflinePlayer owner, @NotNull Map<Integer, Purchasable> content) {
        Preconditions.checkNotNull(owner, "QuickBuy's owner cannot be null!");
        Preconditions.checkNotNull(content, "QuickBuy's content map cannot be null!");

        Map<Integer, Purchasable> purchasables = new HashMap<>(21);
        for (Entry<Integer, Purchasable> entry : content.entrySet()) {
            Purchasable purchasable = entry.getValue();
            if (purchasable == null)
                continue;

            int slot = entry.getKey();
            if (slot < 0 || slot > 20)
                continue;

            purchasables.put(slot, purchasable);
        }

        this.owner = owner;
        this.content = purchasables;
    }

    public BedwarsUserQuickBuy(@NotNull OfflinePlayer owner) {
        Preconditions.checkNotNull(owner, "QuickBuy's owner cannot be null!");

        this.owner = owner;
        this.content = new HashMap<>(21);
    }

    @NotNull
    public static ItemStack getDisplayItem(@NotNull Language lang) {
        return QUICK_BUY_ICONS.get(lang);
    }

    @Override
    public @NotNull NamesRegistry getNames() {
        return QUICK_BUY_NAMES;
    }

    @Override
    public @NotNull ItemStack getDisplayItem(@NotNull GamePlayer player) {
        Preconditions.checkNotNull(player, "Cannot get the display item for a null player!");
        return QUICK_BUY_ICONS.get(MessageUtils.getPlayerLanguage(player.getPlayer()));
    }

    @Override
    public @NotNull Map<Integer, Purchasable> getPurchasables() {
        return new HashMap<>(content);
    }

    @Override
    public @Nullable Purchasable getPurchasable(@Range(from = 0, to = 20) int slot) {
        return content.get(slot);
    }

    @Override
    public @NotNull OfflinePlayer getOwner() {
        return owner;
    }

    @Override
    public boolean setPurchasable(@Range(from = 0, to = 20) int slot, @Nullable Purchasable purchasable) {
        if (slot < 0 || slot > 20)
            return false;

        Purchasable existing = content.get(slot);
        if (existing != null && existing.equals(purchasable))
            return false;

        QuickBuyAction action = existing == null ? purchasable != null ? QuickBuyAction.ADD : QuickBuyAction.REMOVE : QuickBuyAction.REPLACE;

        UserQuickBuyEditEvent event = new UserQuickBuyEditEvent(this, action, purchasable, slot);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return false;

        int old_index = indexOf(purchasable);
        if (old_index != -1)
            this.content.remove(old_index);

        this.content.put(slot, purchasable);
        return true;
    }

    @Override
    public boolean click(@NotNull GamePlayer player, int slot) {
        if (player == null || slot < 0 || slot > 53)
            return false;

        Purchasable purchasable = content.get(toCategorySlot(slot));
        return purchasable != null && purchasable.purchase(player);
    }

    @Override
    public boolean populate(@NotNull Inventory inventory, @NotNull GamePlayer player) {
        if (inventory == null || player == null)
            return false;

        Language lang = MessageUtils.getPlayerLanguage(player.getPlayer());
        for (int i = 0; i < 21; i++) {
            Purchasable purchasable = content.get(i);
            if (purchasable != null)
                inventory.setItem(toInventorySlot(i), getDisplayItem(purchasable, player));
            else
                inventory.setItem(toInventorySlot(i), EMPTY_ICONS.get(lang));
        }

        return true;
    }

    @Override
    public boolean contains(@NotNull Purchasable purchasable) {
        return content.containsValue(purchasable);
    }

    @Override
    public void forEach(@NotNull BiConsumer<Integer, Purchasable> action) {
        if (action == null)
            return;

        for (Entry<Integer, Purchasable> entry : content.entrySet())
            action.accept(entry.getKey(), entry.getValue());
    }

    @Override
    public void forEach(@NotNull Consumer<Purchasable> action) {
        if (action == null)
            return;

        for (Purchasable purchasable : content.values())
            action.accept(purchasable);
    }

    @NotNull
    private ItemStack getDisplayItem(@NotNull Purchasable purchasable, @NotNull GamePlayer player) {
        ItemStack item = purchasable.getDisplayItem(player);
        ItemMeta meta = item.getItemMeta();

        List<String> lore = meta.getLore();
        lore.add(lore.size() - 1, MessageUtils.formatLangMessage(Message.SHOP_DISPLAY_QUICK_BUY_INTERACTION_REMOVE, player.getPlayer()));

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @Range(from = -1, to = 53)
    private int indexOf(@NotNull Purchasable purchasable) {
        for (Entry<Integer, Purchasable> entry : content.entrySet()) {
            Purchasable existing = entry.getValue();
            if (existing != null && existing.equals(purchasable))
                return entry.getKey();
        }

        return -1;
    }

    @Range(from = -1, to = 20)
    public int toCategorySlot(@Range(from = 0, to = 53) int slot) {
        if (slot < 0 || slot > 53)
            return -1;

        if (slot >= 19 && slot <= 25)
            return slot - 19;

        if (slot >= 28 && slot <= 34)
            return slot - 21;

        if (slot >= 37 && slot <= 43)
            return slot - 23;

        return -1;
    }

    @Range(from = -1, to = 53)
    private int toInventorySlot(@Range(from = 0, to = 20) int slot) {
        if (slot >= 0 && slot <= 6)
            return slot + 19;

        if (slot >= 7 && slot <= 13)
            return slot + 21;

        if (slot >= 14 && slot <= 20)
            return slot + 23;

        return -1;
    }

}