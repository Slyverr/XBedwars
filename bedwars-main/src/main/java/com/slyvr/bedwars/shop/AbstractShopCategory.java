package com.slyvr.bedwars.shop;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.lang.Language;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.api.lang.NamesRegistry;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.api.shop.ShopCategory;
import com.slyvr.bedwars.api.shop.content.Purchasable;
import com.slyvr.bedwars.utils.ItemManager;
import com.slyvr.bedwars.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AbstractShopCategory implements ShopCategory {

    protected final Map<Integer, Purchasable> content;
    protected final Map<Language, ItemStack> icons;

    protected final NamesRegistry names;
    protected final ItemStack icon;

    public AbstractShopCategory(@NotNull NamesRegistry names, @NotNull ItemStack icon, @Nullable Map<Integer, Purchasable> content) {
        Preconditions.checkNotNull(names, "Category's names holder cannot be null!");
        Preconditions.checkNotNull(icon, "Category's icon cannot be null!");

        if (content == null)
            content = Collections.EMPTY_MAP;

        Map<Integer, Purchasable> purchasables = new HashMap<>(content.size(), 1F);
        for (Entry<Integer, Purchasable> entry : content.entrySet()) {
            Purchasable purchasable = entry.getValue();
            if (purchasable == null)
                continue;

            int slot = entry.getKey();
            if (slot < 0 || slot > 20)
                continue;

            purchasables.put(slot, purchasable);
        }

        Map<Language, String> names_map = names.getNames();
        Map<Language, ItemStack> icons = new HashMap<>(names_map.size(), 1F);

        for (Entry<Language, String> entry : names_map.entrySet())
            icons.put(entry.getKey(), getDisplayItem(icon, entry.getValue(), entry.getKey()));

        this.icon = getDisplayItem(icon, names.getName(Language.ENGLISH), Language.ENGLISH);

        this.content = Collections.unmodifiableMap(purchasables);
        this.icons = Collections.unmodifiableMap(icons);
        this.names = names;
    }

    public AbstractShopCategory(@NotNull NamesRegistry names, @NotNull ItemStack icon) {
        this(names, icon, Collections.EMPTY_MAP);
    }

    @NotNull
    protected ItemStack getDisplayItem(@NotNull ItemStack icon, @NotNull String name, @NotNull Language lang) {
        return new ItemManager(icon)
                .setDisplayName(ChatColor.GREEN + name)
                .addLore(MessageUtils.formatLangMessage(Message.INTERACTION_CLICK_TO_VIEW, lang))
                .getItemStack();
    }

    @Override
    public @NotNull NamesRegistry getNames() {
        return names;
    }

    @Override
    public @NotNull ItemStack getDisplayItem(@NotNull GamePlayer player) {
        Preconditions.checkNotNull(player, "Cannot get the display item for a null player!");
        return icons.getOrDefault(MessageUtils.getPlayerLanguage(player.getPlayer()), icon).clone();
    }

    @Override
    public @NotNull Map<Integer, Purchasable> getPurchasables() {
        return content;
    }

    @Override
    public @Nullable Purchasable getPurchasable(@Range(from = 0, to = 20) int slot) {
        return content.get(slot);
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof AbstractShopCategory))
            return false;

        AbstractShopCategory other = (AbstractShopCategory) obj;
        return content.equals(other.content) && names.equals(other.names) && icon.equals(other.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, icons, names, icon);
    }

}