package com.slyvr.xbedwars.commands.subcommands.lobby;

import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.lobby.Lobby;
import com.slyvr.xbedwars.api.server.ServerType;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.configuration.LobbiesConfig;
import com.slyvr.xbedwars.lobby.BedwarsWorldLobby;
import com.slyvr.xbedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;


public final class SetLobbyCommand extends SubCommand {

    public SetLobbyCommand() {
        super("setLobby", "Adds a new lobby to xbedwars!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw setLobby <name> <capacity-opt>";
    }

    @Override
    public void perform(@NotNull Player player, String[] args) {
        if (args.length < 2) {
            CommandUtils.sendUsage(this, player);
            return;
        }

        ServerType type = ServerType.DEFAULT;
        if (type != ServerType.DEFAULT) {
            CommandUtils.sendMessage(Message.COMMAND_FAILURE_LOBBY_CREATION, player);
            return;
        }

        Lobby existing = LobbiesConfig.getInstance().getLobby(args[1]);
        if (existing != null) {
            CommandUtils.sendMessage(Message.LOBBY_EXISTS, player);
            return;
        }

        LobbiesConfig.getInstance().setLobby(new BedwarsWorldLobby(args[1], player.getLocation(), args.length >= 3 ? NumberConversions.toInt(args[2]) : Integer.MAX_VALUE));
        CommandUtils.sendMessage(Message.LOBBY_CREATED, player, args[1]);
    }

}
