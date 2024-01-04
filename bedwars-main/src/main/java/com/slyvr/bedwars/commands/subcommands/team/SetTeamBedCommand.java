package com.slyvr.bedwars.commands.subcommands.team;

import com.slyvr.bedwars.api.arena.Arena;
import com.slyvr.bedwars.api.arena.team.ArenaTeam;
import com.slyvr.bedwars.api.arena.team.ArenaTeamBed;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.api.team.TeamColor;
import com.slyvr.bedwars.arena.BedwarsArenaTeamBed;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.utils.BedUtils;
import com.slyvr.bedwars.utils.CommandUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class SetTeamBedCommand extends SubCommand {

    public SetTeamBedCommand() {
        super("setTeamBed", "Sets the bed of a team in an arena!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw setTeamBed <arena> <color>";
    }

    @Override
    public void perform(@NotNull Player player, @NotNull String[] args) {
        if (args.length < 3) {
            CommandUtils.sendUsage(this, player);
            return;
        }

        Arena arena = CommandUtils.getArena(args[1], player);
        if (arena == null)
            return;

        TeamColor color = CommandUtils.getTeamColor(args[2], player);
        if (color == null)
            return;

        Block part = player.getLocation().getBlock();
        if (!BedUtils.isBed(part.getType())) {
            CommandUtils.sendMessage(Message.ARENA_INVALID_BED_BLOCK, player);
            return;
        }

        ArenaTeamBed bed = BedwarsArenaTeamBed.create(part, color);
        if (bed == null)
            return;

        ArenaTeam arena_team = arena.getTeam(color);
        arena_team.setBed(bed);

        CommandUtils.sendArenaMessage(Message.ARENA_MODIFICATION_TEAM_BED, player, arena, color.getColoredName());
    }

}