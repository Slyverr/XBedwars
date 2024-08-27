package com.slyvr.xbedwars.commands.subcommands.lobby;

import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.configuration.LobbiesConfig;
import com.slyvr.xbedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class RemoveLobbyCommand extends SubCommand {

    public RemoveLobbyCommand() {
        super("removeLobby", "Removes an existing lobby from xbedwars!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw removeLobby <name>";
    }

    @Override
    public void perform(@NotNull Player player, String[] args) {
        if (args.length < 2) {
            CommandUtils.sendUsage(this, player);
            return;
        }

        if (!LobbiesConfig.getInstance().remove(args[1]))
            CommandUtils.sendMessage(Message.LOBBY_MISSING, player, args[1]);
        else
            CommandUtils.sendMessage(Message.LOBBY_REMOVED, player);
    }

}
