package com.slyvr.xbedwars.v1_12_R1.utils;

import com.slyvr.xbedwars.api.entity.GameEntityType;
import com.slyvr.xbedwars.api.npc.ShopNPCType;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.v1_12_R1.entity.BedBug;
import com.slyvr.xbedwars.v1_12_R1.entity.BodyGuard;
import com.slyvr.xbedwars.v1_12_R1.npc.Villager;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EntityTypes;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityEquipment;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public final class XBedwarsUtils implements com.slyvr.xbedwars.api.XBedwarsUtils {

    static {
        try {
            ShopNPCType.register(Villager.VILLAGER_NPC_TYPE);

            GameEntityType.register(BodyGuard.BODY_GUARD_TYPE);
            GameEntityType.register(BedBug.BED_BUG_TYPE);

        } catch (IllegalArgumentException ignored) {
        }
    }

    public XBedwarsUtils() {
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
                XBedwarsUtils.sendPackets(other.getPlayer(), boots, leggings, chestplate, helmet);
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

        player.getGame().forEach(other -> XBedwarsUtils.sendPackets(other.getPlayer(), boots, leggings, chestplate, helmet));
    }

    @Override
    public void register() {
        XBedwarsUtils.registerEntity("BedBug", 60, BedBug.class);
        XBedwarsUtils.registerEntity("BodyGuard", 99, BodyGuard.class);
        XBedwarsUtils.registerEntity("Villager", 120, Villager.class);
    }

    private static void registerEntity(@NotNull String name, int id, @NotNull Class<? extends Entity> custom) {
        EntityTypes.b.a(id, new MinecraftKey(name), custom);
    }

    private static void sendPackets(@NotNull Player player, Packet<?>... packets) {
        EntityPlayer nms_player = ((CraftPlayer) player).getHandle();

        for (Packet<?> packet : packets)
            nms_player.playerConnection.sendPacket(packet);
    }

}