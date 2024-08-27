package com.slyvr.xbedwars.commands.subcommands.utils;

import com.slyvr.xbedwars.api.lang.Language;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class LangsCommand extends SubCommand {

    private static final String TEXT = CommandUtils.format(Language.values(), Language::getName, "");

    public LangsCommand() {
        super("langs", "Shows all available languages!", "bw.commands.lang");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw langs";
    }

    @Override
    public void perform(@NotNull Player player, @NotNull String[] args) {
        player.sendMessage(TEXT);
    }

}