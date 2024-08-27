package com.slyvr.xbedwars.v1_19_R2.utils;

import com.mojang.datafixers.util.Pair;
import com.slyvr.xbedwars.api.entity.GameEntityType;
import com.slyvr.xbedwars.api.npc.ShopNPCType;
import com.slyvr.xbedwars.api.player.GamePlayer;
import com.slyvr.xbedwars.v1_19_R2.entity.BedBug;
import com.slyvr.xbedwars.v1_19_R2.entity.BodyGuard;
import com.slyvr.xbedwars.v1_19_R2.npc.Villager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class XBedwarsUtils implements com.slyvr.xbedwars.api.XBedwarsUtils {

    private static final List<Pair<EquipmentSlot, ItemStack>> EMPTY_EQUIPMENT = Arrays.asList(
            new Pair<>(EquipmentSlot.FEET, null),
            new Pair<>(EquipmentSlot.LEGS, null),
            new Pair<>(EquipmentSlot.CHEST, null),
            new Pair<>(EquipmentSlot.HEAD, null)
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

        ClientboundSetEquipmentPacket empty_equipment = new ClientboundSetEquipmentPacket(player.getPlayer().getEntityId(), EMPTY_EQUIPMENT);

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

        List<Pair<EquipmentSlot, ItemStack>> equipment = new ArrayList<>(4);
        equipment.add(Pair.of(EquipmentSlot.FEET, CraftItemStack.asNMSCopy(inv.getBoots())));
        equipment.add(Pair.of(EquipmentSlot.LEGS, CraftItemStack.asNMSCopy(inv.getLeggings())));
        equipment.add(Pair.of(EquipmentSlot.CHEST, CraftItemStack.asNMSCopy(inv.getChestplate())));
        equipment.add(Pair.of(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(inv.getHelmet())));

        ClientboundSetEquipmentPacket full_equipment = new ClientboundSetEquipmentPacket(entity_id, equipment);

        player.getGame().forEach(other -> XBedwarsUtils.sendPackets(other.getPlayer(), full_equipment));
    }

    public void register() {
    }

    private static void sendPackets(@NotNull Player player, Packet<?>... packets) {
        ServerPlayer nms_player = ((CraftPlayer) player).getHandle();

        for (Packet<?> packet : packets)
            nms_player.connection.send(packet);
    }

}