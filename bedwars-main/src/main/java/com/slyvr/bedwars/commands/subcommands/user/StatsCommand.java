package com.slyvr.bedwars.commands.subcommands.user;

import com.slyvr.bedwars.commands.subcommands.SubCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class StatsCommand extends SubCommand {

    public StatsCommand() {
        super("stats", "Shows the statistics of the user!", "bw.commands.stats");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw stats <mode>";
    }

    @Override
    public void perform(@NotNull Player player, @NotNull String[] args) {
    }

}