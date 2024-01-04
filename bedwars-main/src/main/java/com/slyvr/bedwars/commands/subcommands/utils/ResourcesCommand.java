package com.slyvr.bedwars.commands.subcommands.utils;

import com.slyvr.bedwars.api.generator.Resource;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;


public final class ResourcesCommand extends SubCommand {

    public static final Function<Resource, String> function = resource -> !resource.equals(Resource.FREE) ? resource.getColoredName() : null;

    public ResourcesCommand() {
        super("resources", "Show all available bedwars resources!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw resources";
    }

    @Override
    public void perform(@NotNull Player player, String[] args) {
        player.sendMessage(CommandUtils.format(Resource.values(), function, ""));
    }

}