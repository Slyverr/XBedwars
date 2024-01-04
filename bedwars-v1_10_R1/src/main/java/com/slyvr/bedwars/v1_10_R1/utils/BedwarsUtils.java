package com.slyvr.bedwars.v1_10_R1.utils;

import com.slyvr.bedwars.api.BedwarsPluginUtils;
import com.slyvr.bedwars.api.entity.GameEntityType;
import com.slyvr.bedwars.api.npc.ShopNPCType;
import com.slyvr.bedwars.api.player.GamePlayer;
import com.slyvr.bedwars.v1_10_R1.entity.BedBug;
import com.slyvr.bedwars.v1_10_R1.entity.BodyGuard;
import com.slyvr.bedwars.v1_10_R1.npc.Villager;
import net.minecraft.server.v1_10_R1.*;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BedwarsUtils implements BedwarsPluginUtils {

    static {
        try {
            ShopNPCType.register(Villager.VILLAGER_NPC_TYPE);

            GameEntityType.register(BodyGuard.BODY_GUARD_TYPE);
            GameEntityType.register(BedBug.BED_BUG_TYPE);

        } catch (IllegalArgumentException ignored) {
        }
    }

    public BedwarsUtils() {
    }

    private static void registerEntity(@NotNull String name, int id, @NotNull Class<?> nms, @NotNull Class<?> custom) {
        Class<?> entity_types_class = EntityTypes.class;

        try {
            List<Map<?, ?>> dataMap = new ArrayList<>();
            for (Field f : entity_types_class.getDeclaredFields()) {
                if (f.getType().getSimpleName().equals(Map.class.getSimpleName())) {
                    f.setAccessible(true);
                    dataMap.add((Map<?, ?>) f.get(null));
                }
            }

            if (dataMap.get(2).containsKey(id)) {
                dataMap.get(0).remove(name);
                dataMap.get(2).remove(id);
            }

            Method method = entity_types_class.getDeclaredMethod("a", Class.class, String.class, int.class);
            method.setAccessible(true);
            method.invoke(null, custom, name, id);

        } catch (Exception ignored) {

        }

    }

    private static void sendPackets(@NotNull Player player, Packet<?>... packets) {
        EntityPlayer nms_player = ((CraftPlayer) player).getHandle();

        for (Packet<?> packet : packets)
            nms_player.playerConnection.sendPacket(packet);
    }

    @Override
    public void hidePlayerArmor(@NotNull GamePlayer player) {
        if (player == null)
            return;

        Player bukkit_player = player.getPlayer();
        int entity_id = bukkit_player.getEntityId();

        PacketPlayOutEntityEquipment boots = new PacketPlayOutEntityEquipment(entity_id, EnumItemSlot.FEET, null);
        PacketPlayOutEntityEquipment leggings = new PacketPlayOutEntityEquipment(entity_id, EnumItemSlot.LEGS, null);
        PacketPlayOutEntityEquipment chestplate = new PacketPlayOutEntityEquipment(entity_id, EnumItemSlot.CHEST, null);
        PacketPlayOutEntityEquipment helmet = new PacketPlayOutEntityEquipment(entity_id, EnumItemSlot.HEAD, null);

        player.getGame().forEach(other -> {
            if (other.getTeamColor() != player.getTeamColor())
                BedwarsUtils.sendPackets(other.getPlayer(), boots, leggings, chestplate, helmet);
        });
    }

    @Override
    public void showPlayerArmor(@NotNull GamePlayer player) {
        if (player == null)
            return;

        Player bukkit_player = player.getPlayer();
        PlayerInventory inv = bukkit_player.getInventory();
        int entity_id = bukkit_player.getEntityId();

        PacketPlayOutEntityEquipment boots = new PacketPlayOutEntityEquipment(entity_id, EnumItemSlot.FEET, CraftItemStack.asNMSCopy(inv.getBoots()));
        PacketPlayOutEntityEquipment leggings = new PacketPlayOutEntityEquipment(entity_id, EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(inv.getLeggings()));
        PacketPlayOutEntityEquipment chestplate = new PacketPlayOutEntityEquipment(entity_id, EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(inv.getChestplate()));
        PacketPlayOutEntityEquipment helmet = new PacketPlayOutEntityEquipment(entity_id, EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(inv.getHelmet()));

        player.getGame().forEach(other -> BedwarsUtils.sendPackets(other.getPlayer(), boots, leggings, chestplate, helmet));
    }

    @Override
    public void register() {
        BedwarsUtils.registerEntity("BedBug", 60, EntitySilverfish.class, BedBug.class);
        BedwarsUtils.registerEntity("BodyGuard", 99, EntityIronGolem.class, BodyGuard.class);
        BedwarsUtils.registerEntity("Villager", 120, EntityVillager.class, Villager.class);
    }

}