package com.slyvr.xbedwars.commands.subcommands.utils;

import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class MapSelectorCommand extends SubCommand {

    public MapSelectorCommand() {
        super("maps", "Shows all available games!", "bw.commands.maps");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw maps";
    }

    @Override
    public void perform(@NotNull Player player, String[] args) {
        // TODO: Make the map selector
    }

}