package com.slyvr.xbedwars.commands.subcommands.utils;

import com.slyvr.xbedwars.api.game.GameMode;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class ModesCommand extends SubCommand {

    public ModesCommand() {
        super("modes", "Shows all available xbedwars modes!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw modes";
    }

    @Override
    public void perform(@NotNull Player player, @NotNull String[] args) {
        player.sendMessage(CommandUtils.format(GameMode.values(), GameMode::getName, ""));
    }

}