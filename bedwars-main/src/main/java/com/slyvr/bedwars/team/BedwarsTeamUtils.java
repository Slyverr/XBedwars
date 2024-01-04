package com.slyvr.bedwars.team;

import com.cryptomorin.xseries.XMaterial;
import com.slyvr.bedwars.api.player.ArmorType;
import com.slyvr.bedwars.api.team.TeamColor;
import com.slyvr.bedwars.utils.ItemManager;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;


public final class BedwarsTeamUtils {

    private BedwarsTeamUtils() {
    }

    @NotNull
    public static ItemStack getColoredBlock(@NotNull ItemStack item, @NotNull TeamColor color) {
        Material type = item.getType();
        if (!type.isBlock())
            return item;

        String type_name = type.toString();
        if (type_name.endsWith("WOOL"))
            return BedwarsTeamUtils.getTeamColoredWool(color).setType(item);

        if (type_name.endsWith("STAINED_GLASS"))
            return BedwarsTeamUtils.getTeamColoredGlass(color).setType(item);

        if (type_name.endsWith("STAINED_GLASS_PANE"))
            return BedwarsTeamUtils.getTeamColoredGlassPane(color).setType(item);

        if (type_name.endsWith("TERRACOTTA") || type_name.equals("HARD_CLAY"))
            return BedwarsTeamUtils.getTeamColoredTerracotta(color).setType(item);

        return item;
    }

    @NotNull
    public static XMaterial getColoredBlock(@NotNull XMaterial type, @NotNull TeamColor color) {
        Material bukkit_type = type.parseMaterial();
        if (!bukkit_type.isBlock())
            return type;

        String type_name = bukkit_type.name();
        if (type_name.endsWith("WOOL"))
            return BedwarsTeamUtils.getTeamColoredWool(color);

        if (type_name.endsWith("STAINED_GLASS"))
            return BedwarsTeamUtils.getTeamColoredGlass(color);

        if (type_name.endsWith("STAINED_GLASS_PANE"))
            return BedwarsTeamUtils.getTeamColoredGlassPane(color);

        if (type_name.endsWith("TERRACOTTA") || type_name.equals("HARD_CLAY"))
            return BedwarsTeamUtils.getTeamColoredTerracotta(color);

        return type;
    }

    @NotNull
    public static XMaterial getTeamColoredWool(@NotNull TeamColor color) {
        switch (color) {
            case RED:
                return XMaterial.RED_WOOL;
            case BLUE:
                return XMaterial.BLUE_WOOL;
            case GREEN:
                return XMaterial.LIME_WOOL;
            case YELLOW:
                return XMaterial.YELLOW_WOOL;
            case AQUA:
                return XMaterial.CYAN_WOOL;
            case WHITE:
                return XMaterial.WHITE_WOOL;
            case PINK:
                return XMaterial.PINK_WOOL;
            case GRAY:
                return XMaterial.GRAY_WOOL;
        }

        return null;
    }

    @NotNull
    public static XMaterial getTeamColoredGlass(@NotNull TeamColor color) {

        switch (color) {
            case RED:
                return XMaterial.RED_STAINED_GLASS;
            case BLUE:
                return XMaterial.BLUE_STAINED_GLASS;
            case GREEN:
                return XMaterial.LIME_STAINED_GLASS;
            case YELLOW:
                return XMaterial.YELLOW_STAINED_GLASS;
            case AQUA:
                return XMaterial.CYAN_STAINED_GLASS;
            case WHITE:
                return XMaterial.WHITE_STAINED_GLASS;
            case PINK:
                return XMaterial.PINK_STAINED_GLASS;
            case GRAY:
                return XMaterial.GRAY_STAINED_GLASS;
        }

        return null;
    }

    @NotNull
    public static XMaterial getTeamColoredGlassPane(@NotNull TeamColor color) {

        switch (color) {
            case RED:
                return XMaterial.RED_STAINED_GLASS_PANE;
            case BLUE:
                return XMaterial.BLUE_STAINED_GLASS_PANE;
            case GREEN:
                return XMaterial.LIME_STAINED_GLASS_PANE;
            case YELLOW:
                return XMaterial.YELLOW_STAINED_GLASS_PANE;
            case AQUA:
                return XMaterial.CYAN_STAINED_GLASS_PANE;
            case WHITE:
                return XMaterial.WHITE_STAINED_GLASS_PANE;
            case PINK:
                return XMaterial.PINK_STAINED_GLASS_PANE;
            case GRAY:
                return XMaterial.GRAY_STAINED_GLASS_PANE;
        }

        return null;
    }

    @NotNull
    public static XMaterial getTeamColoredTerracotta(@NotNull TeamColor color) {
        switch (color) {
            case RED:
                return XMaterial.RED_TERRACOTTA;
            case BLUE:
                return XMaterial.BLUE_TERRACOTTA;
            case GREEN:
                return XMaterial.LIME_TERRACOTTA;
            case YELLOW:
                return XMaterial.YELLOW_TERRACOTTA;
            case AQUA:
                return XMaterial.CYAN_TERRACOTTA;
            case WHITE:
                return XMaterial.WHITE_TERRACOTTA;
            case PINK:
                return XMaterial.PINK_TERRACOTTA;
            case GRAY:
                return XMaterial.GRAY_TERRACOTTA;
        }

        return null;
    }

    public static void setPlayerArmor(@NotNull Player player, @NotNull ArmorType type, @NotNull TeamColor team) {
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack leggings = new ItemStack(type.getLeggings());
        ItemStack boots = new ItemStack(type.getBoots());

        Color color = team.getDyeColor().getColor();
        helmet.addEnchantment(Enchantment.WATER_WORKER, 1);

        BedwarsTeamUtils.checkPiece(helmet, color);
        BedwarsTeamUtils.checkPiece(chestplate, color);
        BedwarsTeamUtils.checkPiece(leggings, color);
        BedwarsTeamUtils.checkPiece(boots, color);

        EntityEquipment equipment = player.getEquipment();

        equipment.setHelmet(helmet);
        equipment.setChestplate(chestplate);
        equipment.setLeggings(leggings);
        equipment.setBoots(boots);
    }

    private static void checkPiece(@NotNull ItemStack item, @NotNull Color color) {
        ItemManager manager = new ItemManager(item);
        manager.setUnbreakable(true);

        ItemMeta meta = manager.getItemMeta();
        if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leatherMeta = (LeatherArmorMeta) meta;
            leatherMeta.setColor(color);
        }

        item.setItemMeta(meta);
    }

}