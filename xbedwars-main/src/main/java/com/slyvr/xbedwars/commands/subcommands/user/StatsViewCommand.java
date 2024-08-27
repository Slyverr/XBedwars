package com.slyvr.xbedwars.commands.subcommands.user;

import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class StatsViewCommand extends SubCommand {

    public StatsViewCommand() {
        super("viewStats", "Shows the statistics of other players!", "bw.commands.stats.view");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw viewStats <player> <mode>";
    }

    @Override
    public void perform(@NotNull Player player, @NotNull String[] args) {

    }

}