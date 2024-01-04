package com.slyvr.bedwars.arena;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.arena.team.ArenaTeamBed;
import com.slyvr.bedwars.api.team.TeamColor;
import com.slyvr.bedwars.utils.BedUtils;
import com.slyvr.bedwars.utils.Version;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.bukkit.block.data.type.Bed.Part;


public final class BedwarsArenaTeamBed implements ArenaTeamBed {

    private final TeamColor color;

    private final Block head;
    private final Block foot;

    private final BlockFace facing;

    private final Material type;


    private BedwarsArenaTeamBed(@NotNull TeamColor color, @NotNull Block head, @NotNull Block foot, @NotNull BlockFace facing) {
        this.color = color;
        this.head = head;
        this.foot = foot;

        this.facing = facing;
        this.type = foot.getType();
    }

    @Nullable
    public static ArenaTeamBed create(@NotNull Block block, @NotNull TeamColor color) {
        Preconditions.checkNotNull(block, "Bed's block cannot be null!");
        Preconditions.checkNotNull(color, "Bed's team color cannot be null!");

        if (Version.getVersion().isNewAPI()) {
            BlockData data = block.getBlockData();
            if (!(data instanceof Bed))
                return null;

            Bed bed = (Bed) data;
            if (bed.getPart() == Part.HEAD)
                return new BedwarsArenaTeamBed(color, block, block.getRelative(bed.getFacing().getOppositeFace()), bed.getFacing());
            else
                return new BedwarsArenaTeamBed(color, block.getRelative(bed.getFacing()), block, bed.getFacing());
        }

        MaterialData data = block.getState().getData();
        if (!(data instanceof org.bukkit.material.Bed))
            return null;

        org.bukkit.material.Bed bed = (org.bukkit.material.Bed) data;
        if (bed.isHeadOfBed())
            return new BedwarsArenaTeamBed(color, block, block.getRelative(bed.getFacing().getOppositeFace()), bed.getFacing());
        else
            return new BedwarsArenaTeamBed(color, block.getRelative(bed.getFacing()), block, bed.getFacing());
    }

    @Override
    @NotNull
    public TeamColor getTeamColor() {
        return color;
    }

    @Override
    @NotNull
    public Block getHead() {
        return head;
    }

    @Override
    @NotNull
    public Block getFoot() {
        return foot;
    }

    @Override
    @NotNull
    public BlockFace getFacing() {
        return facing;
    }

    @Override
    public void place() {
        BedUtils.placeBed(foot, facing, type);
    }

    @Override
    public boolean destroy() {
        if (!BedUtils.isBed(head, foot))
            return false;

        foot.setType(Material.AIR, false);
        head.setType(Material.AIR, false);
        return true;
    }

    @Override
    public boolean isDestroyed() {
        return !BedUtils.isBed(head, foot);
    }

}