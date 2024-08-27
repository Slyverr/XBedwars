package com.slyvr.xbedwars.commands.subcommands.team;

import com.slyvr.xbedwars.api.arena.Arena;
import com.slyvr.xbedwars.api.arena.team.ArenaTeam;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.shop.Shop.ShopType;
import com.slyvr.xbedwars.api.team.TeamColor;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class SetTeamUpgradeCommand extends SubCommand {

    public SetTeamUpgradeCommand() {
        super("setTeamUpgrade", "Sets the upgrade's shop of a team in an arena!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw setTeamUpgrade <arena> <color>";
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

        ArenaTeam arena_team = arena.getTeam(color);
        arena_team.setShopNPCLocation(ShopType.UPGRADES, player.getLocation());

        CommandUtils.sendArenaMessage(Message.ARENA_MODIFICATION_TEAM_SHOP_UPGRADES, player, arena, color.getColoredName());
    }

}
