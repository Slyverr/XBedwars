package com.slyvr.xbedwars.commands.subcommands.team;

import com.slyvr.xbedwars.api.arena.Arena;
import com.slyvr.xbedwars.api.arena.team.ArenaTeam;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.team.TeamColor;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.utils.CommandUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class SetTeamChestCommand extends SubCommand {

    public SetTeamChestCommand() {
        super("setTeamChest", "Sets the chest of a team in an arena!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw setTeamChest <arena> <color>";
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

        Block block = player.getLocation().getBlock();
        if (block.getType() != Material.CHEST) {
            CommandUtils.sendMessage(Message.ARENA_INVALID_CHEST_BLOCK, player);
            return;
        }

        ArenaTeam arena_team = arena.getTeam(color);
        arena_team.setChest((Chest) block.getState());

        CommandUtils.sendArenaMessage(Message.ARENA_MODIFICATION_TEAM_CHEST, player, arena, color.getColoredName());
    }

}
