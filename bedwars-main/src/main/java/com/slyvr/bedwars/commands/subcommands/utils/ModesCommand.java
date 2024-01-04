package com.slyvr.bedwars.commands.subcommands.utils;

import com.slyvr.bedwars.api.game.GameMode;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class ModesCommand extends SubCommand {

    public ModesCommand() {
        super("modes", "Shows all available bedwars modes!", "bw.setup");
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