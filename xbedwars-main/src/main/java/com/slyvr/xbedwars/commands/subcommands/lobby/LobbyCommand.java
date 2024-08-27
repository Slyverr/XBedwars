package com.slyvr.xbedwars.commands.subcommands.lobby;

import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.api.lobby.Lobby;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.configuration.LobbiesConfig;
import com.slyvr.xbedwars.utils.CommandUtils;
import com.slyvr.xbedwars.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class LobbyCommand extends SubCommand {

    public LobbyCommand() {
        super("lobby", "Teleports the player to a lobby!", "bw.commands.lobby");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw lobby <name>";
    }

    @Override
    public void perform(@NotNull Player player, String[] args) {
        if (args.length < 2) {
            CommandUtils.sendUsage(this, player);
            return;
        }

        Lobby lobby = LobbiesConfig.getInstance().getLobby(args[1]);
        if (lobby == null) {
            MessageUtils.sendLangMessage(Message.LOBBY_MISSING, player, args[1]);
            return;
        }

        if (!lobby.send(player))
            MessageUtils.sendLangMessage(Message.LOBBY_TELEPORT_FAILURE, player);
    }

}