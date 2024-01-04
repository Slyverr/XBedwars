package com.slyvr.bedwars;

import com.cryptomorin.xseries.XMaterial;
import com.slyvr.bedwars.api.lang.Language;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.utils.ItemManager;
import com.slyvr.bedwars.utils.MessageUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class BedwarsItems {

    public static final ItemStack SWORD;
    private static final Map<Language, ItemStack> LOBBY_RETURN_ITEMS = new HashMap<>();

    static {
//        BODY_GUARD_SPAWN_EGG = new ItemManager(XMaterial.SPAWNER.parseItem()).setDisplayName(ChatColor.RESET + "Dream Defender (Iron Golem)").getItemStack();
//        BED_BUG_SPAWN_EGG = new ItemManager(XMaterial.SNOWBALL.parseItem()).setDisplayName(ChatColor.RESET + "Bed Bug (Silverfish Snowball)").getItemStack();

        // TODO: Remove those and move them

        SWORD = new ItemManager(XMaterial.WOODEN_SWORD.parseItem()).setUnbreakable(true).getItemStack();

        for (Language lang : Language.values()) {
            ItemManager lobby = new ItemManager(XMaterial.RED_BED.parseItem());
            lobby.setDisplayName(MessageUtils.formatLangMessage(Message.ITEMS_LOBBY_DISPLAY_NAME, lang));
            lobby.addLore(MessageUtils.formatLangMessage(Message.ITEMS_LOBBY_DISPLAY_LORE, lang));

            BedwarsItems.LOBBY_RETURN_ITEMS.put(lang, lobby.getItemStack());
        }

    }

    private BedwarsItems() {
    }

    @NotNull
    public static ItemStack getLobbyReturnItem(@NotNull Language lang) {
        return LOBBY_RETURN_ITEMS.get(lang == null ? Language.ENGLISH : lang);
    }

    public static boolean isLobbyReturnItem(@NotNull ItemStack item) {
        return item != null && LOBBY_RETURN_ITEMS.containsValue(item);
    }


}