package com.slyvr.bedwars.commands.subcommands.lobby;

import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.configuration.LobbiesConfig;
import com.slyvr.bedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class RemoveLobbyCommand extends SubCommand {

    public RemoveLobbyCommand() {
        super("removeLobby", "Removes an existing lobby from bedwars!", "bw.setup");
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
