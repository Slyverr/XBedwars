package com.slyvr.xbedwars.commands.subcommands.utils;

import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.lobby.Lobby;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.configuration.LobbiesConfig;
import com.slyvr.xbedwars.utils.CommandUtils;
import com.slyvr.xbedwars.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class LobbiesCommand extends SubCommand {

    public LobbiesCommand() {
        super("lobbies", "Shows all available lobbies!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw lobbies";
    }

    @Override
    public void perform(@NotNull Player player, @NotNull String[] args) {
        player.sendMessage(CommandUtils.format(LobbiesConfig.getInstance().getLobbies(), Lobby::getName, MessageUtils.formatLangMessage(Message.MISSING_LOBBIES, player)));
    }

}