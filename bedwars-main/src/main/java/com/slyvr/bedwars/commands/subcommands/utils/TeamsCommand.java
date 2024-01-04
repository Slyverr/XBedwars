package com.slyvr.bedwars.commands.subcommands.utils;

import com.slyvr.bedwars.api.team.TeamColor;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class TeamsCommand extends SubCommand {

    private static final String TEXT = CommandUtils.format(TeamColor.values(), TeamColor::getColoredName, "");

    public TeamsCommand() {
        super("teams", "Shows all available team colors!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw teams";
    }

    @Override
    public void perform(@NotNull Player player, @NotNull String[] args) {
        player.sendMessage(TEXT);
    }

}