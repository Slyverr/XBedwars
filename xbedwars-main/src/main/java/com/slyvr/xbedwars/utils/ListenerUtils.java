package com.slyvr.xbedwars.utils;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Spigot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;


public final class ListenerUtils {

    private static final MethodHandle SPIGOT_HANDLE;
    private static final MethodHandle EFFECT_HANDLE;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle spigot_handle = null;
        MethodHandle effect_handle = null;

        if (!Version.getVersion().isNewAPI()) {
            Class<?>[] params = {Location.class, Effect.class, int.class, int.class, float.class, float.class, float.class, float.class, int.class, int.class};

            try {
                spigot_handle = lookup.findVirtual(World.class, "spigot", MethodType.methodType(Spigot.class));
                effect_handle = lookup.findVirtual(Spigot.class, "playEffect", MethodType.methodType(void.class, params));
            } catch (Throwable ignored) {
            }
        }

        SPIGOT_HANDLE = spigot_handle;
        EFFECT_HANDLE = effect_handle;
    }

    private ListenerUtils() {
    }

    public static void playEffect(@NotNull Effect effect, @NotNull Location loc, int count, float offsetX, float offsetY, float offsetZ, float speed) {
        if (effect == null || loc == null)
            return;

        try {
            EFFECT_HANDLE.invoke(SPIGOT_HANDLE.invoke(loc.getWorld()), loc, effect, 0, 0, offsetX, offsetY, offsetZ, speed, count, 0);
        } catch (Throwable ignored) {
        }

    }

    public static void decrementItemInHandAmount(@NotNull Player player, boolean right) {
        ItemStack item = !right && Version.getVersion().isNewerThan(Version.V1_8_R3) ? player.getInventory().getItemInOffHand() : player.getItemInHand();

        int amount = item.getAmount() - 1;
        if (amount > 0) {
            item.setAmount(amount);
            return;
        }

        if (!Version.getVersion().isNewerThan(Version.V1_8_R3)) {
            player.getInventory().setItemInHand(null);
            return;
        }

        ListenerUtils.removeValidHandItem(player, right);
    }

    private static void removeValidHandItem(@NotNull Player player, boolean right) {
        if (right)
            player.getInventory().setItemInMainHand(null);
        else
            player.getInventory().setItemInOffHand(null);
    }

}