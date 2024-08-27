package com.slyvr.xbedwars.v1_16_R2.utils;

import com.mojang.datafixers.util.Pair;
import com.slyvr.xbedwars.api.entity.GameEntityType;
import com.slyvr.xbedwars.api.npc.ShopNPCType;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.v1_16_R2.entity.BedBug;
import com.slyvr.xbedwars.v1_16_R2.entity.BodyGuard;
import com.slyvr.xbedwars.v1_16_R2.npc.Villager;
import net.minecraft.server.v1_16_R2.EntityPlayer;
import net.minecraft.server.v1_16_R2.EnumItemSlot;
import net.minecraft.server.v1_16_R2.ItemStack;
import net.minecraft.server.v1_16_R2.Packet;
import net.minecraft.server.v1_16_R2.PacketPlayOutEntityEquipment;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class XBedwarsUtils implements com.slyvr.xbedwars.api.XBedwarsUtils {

    private static final List<Pair<EnumItemSlot, ItemStack>> EMPTY_EQUIPMENT = Arrays.asList(
            new Pair<>(EnumItemSlot.FEET, null),
            new Pair<>(EnumItemSlot.LEGS, null),
            new Pair<>(EnumItemSlot.CHEST, null),
            new Pair<>(EnumItemSlot.HEAD, null)
    );

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

        PacketPlayOutEntityEquipment empty_equipment = new PacketPlayOutEntityEquipment(player.getPlayer().getEntityId(), EMPTY_EQUIPMENT);

        player.getGame().forEach(other -> {
            if (other.getTeamColor() != player.getTeamColor())
                XBedwarsUtils.sendPackets(other.getPlayer(), empty_equipment);
        });
    }

    @Override
    public void showPlayerArmor(@NotNull GamePlayer player) {
        if (player == null)
            return;

        Player bukkit_player = player.getPlayer();
        PlayerInventory inv = bukkit_player.getInventory();
        int entity_id = bukkit_player.getEntityId();

        List<Pair<EnumItemSlot, ItemStack>> equipment = new ArrayList<>(4);
        equipment.add(Pair.of(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(inv.getBoots())));
        equipment.add(Pair.of(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(inv.getLeggings())));
        equipment.add(Pair.of(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(inv.getChestplate())));
        equipment.add(Pair.of(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(inv.getHelmet())));

        PacketPlayOutEntityEquipment full_equipment = new PacketPlayOutEntityEquipment(entity_id, equipment);

        player.getGame().forEach(other -> XBedwarsUtils.sendPackets(other.getPlayer(), full_equipment));
    }

    public void register() {
    }

    private static void sendPackets(@NotNull Player player, Packet<?>... packets) {
        EntityPlayer nms_player = ((CraftPlayer) player).getHandle();

        for (Packet<?> packet : packets)
            nms_player.playerConnection.sendPacket(packet);
    }

}