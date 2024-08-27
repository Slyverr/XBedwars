package com.slyvr.xbedwars.listener.shop;

import com.slyvr.xbedwars.XBedwars;
import com.slyvr.xbedwars.api.event.shop.ShopClickEvent;
import com.slyvr.xbedwars.api.event.shop.ShopCloseEvent;
import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.api.shop.Shop;
import com.slyvr.xbedwars.api.shop.ShopCategory;
import com.slyvr.xbedwars.api.shop.content.Purchasable;
import com.slyvr.xbedwars.api.user.shop.UserQuickBuy;
import com.slyvr.xbedwars.game.XBedwarsGame;
import com.slyvr.xbedwars.listener.player.GamePlayerItemListener;
import com.slyvr.xbedwars.manager.BedwarsUsersManager;
import com.slyvr.xbedwars.shop.ShopInventory;
import com.slyvr.xbedwars.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class ShopListener implements Listener {

    private static final Map<Inventory, QuickBuyEditor> OPENED_EDITORS = new HashMap<>();

    @EventHandler
    public void onShopInteract(@NotNull InventoryClickEvent event) {
        if (event.getSlotType() == SlotType.OUTSIDE)
            return;

        ShopInventory shop_inv = ShopInventory.getShopInventory(event.getInventory());
        if (shop_inv == null)
            return;

        Player bukkit_player = (Player) event.getWhoClicked();
        Game game = XBedwarsGame.getPlayerGame(bukkit_player);
        if (game == null || !game.isRunning())
            return;

        Shop<?> shop = shop_inv.getShop();
        ShopCategory category = shop_inv.getCategory();

        if (event.getClickedInventory() == event.getView().getBottomInventory()) {
            event.setCancelled(true);
            return;
        }

        GamePlayer player = game.getGamePlayer(bukkit_player);
        int slot = event.getSlot();

        if (event.getClick().isShiftClick() && category.toCategorySlot(slot) != -1) {
            this.handlePurchasableRightClick(player, shop, category, slot);
            event.setCancelled(true);
            return;
        }

        ShopClickEvent bwEvent = new ShopClickEvent(shop, category, player, slot);
        Bukkit.getPluginManager().callEvent(bwEvent);

        if (bwEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        try {
            switch (shop.getType()) {
                case ITEMS:
                    if (shop.click(player, slot))
                        break;

                    category.click(player, slot);
                    shop.open(player, category);
                    break;
                case UPGRADES:
                    if (shop.click(player, slot) || shop == category) {
                        shop.open(player);
                        break;
                    }

                    category.click(player, slot);
                    shop.open(player, category);
                    break;
            }

        } catch (Exception ignored) {

        }

        GamePlayerItemListener.checkForSword(player);
        event.setCancelled(true);
    }

    @EventHandler
    public void onShopClose(@NotNull InventoryCloseEvent event) {
        ShopInventory shop_inv = ShopInventory.getShopInventory(event.getInventory());
        if (shop_inv == null)
            return;

        Player bukkit_player = (Player) event.getPlayer();
        Game game = XBedwarsGame.getPlayerGame(bukkit_player);
        if (game == null)
            return;

        ShopInventory.removeShopInventory(event.getInventory());
        Bukkit.getPluginManager().callEvent(new ShopCloseEvent(shop_inv.getShop(), shop_inv.getCategory(), game.getGamePlayer(bukkit_player)));
    }

    @EventHandler
    public void onShopEditorInteract(@NotNull InventoryClickEvent event) {
        if (event.getSlotType() == SlotType.OUTSIDE)
            return;

        QuickBuyEditor editor = OPENED_EDITORS.get(event.getInventory());
        if (editor == null)
            return;

        if (event.getClickedInventory() == event.getView().getBottomInventory()) {
            event.setCancelled(true);
            return;
        }

        if (editor.apply(event.getSlot()))
            Bukkit.getScheduler().runTask(XBedwars.getInstance(), () -> event.getView().close());

        event.setCancelled(true);
    }

    @EventHandler
    public void onShopEditorClose(@NotNull InventoryCloseEvent event) {
        ShopListener.OPENED_EDITORS.remove(event.getInventory());
    }


    private void handlePurchasableRightClick(@NotNull GamePlayer player, @NotNull Shop shop, @NotNull ShopCategory category, int slot) {
        if (category instanceof UserQuickBuy) {
            UserQuickBuy quick_buy = (UserQuickBuy) category;
            quick_buy.setPurchasable(quick_buy.toCategorySlot(slot), null);

            shop.open(player);
            return;
        }

        QuickBuyEditor editor = new QuickBuyEditor(player, category, slot);
        editor.open();
    }

    private static final class QuickBuyEditor {

        private final UserQuickBuy quick_buy;
        private final Purchasable target;
        private final GamePlayer player;

        public QuickBuyEditor(@NotNull GamePlayer owner, @NotNull ShopCategory category, int slot) {
            this.quick_buy = BedwarsUsersManager.getInstance().getOfflineUser(owner.getPlayer()).getQuickBuy(owner.getGame().getMode());

            this.player = owner;
            this.target = category.getPurchasable(category.toCategorySlot(slot));
        }

        public void open() {
            Inventory inv = Bukkit.createInventory(null, 54, MessageUtils.formatLangMessage(Message.SHOP_DISPLAY_QUICK_BUY_EDITOR_TITLE, player.getPlayer()));
            quick_buy.populate(inv, player);

            player.getPlayer().openInventory(inv);
            OPENED_EDITORS.put(inv, this);
        }

        public boolean apply(int slot) {
            return quick_buy.setPurchasable(quick_buy.toCategorySlot(slot), target);
        }

    }

}