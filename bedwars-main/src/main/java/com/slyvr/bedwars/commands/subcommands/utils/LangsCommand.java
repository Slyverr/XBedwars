package com.slyvr.bedwars.commands.subcommands.utils;

import com.slyvr.bedwars.api.lang.Language;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.utils.CommandUtils;
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