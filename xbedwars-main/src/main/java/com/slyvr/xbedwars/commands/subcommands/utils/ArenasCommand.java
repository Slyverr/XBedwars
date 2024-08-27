package com.slyvr.xbedwars.commands.subcommands.utils;

import com.slyvr.xbedwars.api.arena.Arena;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.manager.BedwarsArenasManager;
import com.slyvr.xbedwars.utils.CommandUtils;
import com.slyvr.xbedwars.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;


public final class ArenasCommand extends SubCommand {


    public ArenasCommand() {
        super("arenas", "Shows all available xbedwars arenas!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw arenas";
    }

    @Override
    public void perform(@NotNull Player player, @NotNull String[] args) {
        Collection<Arena> arenas = BedwarsArenasManager.getInstance().getArenas();
        Arena[] array = arenas.toArray(new Arena[arenas.size()]);

        player.sendMessage(CommandUtils.format(array, Arena::getName, MessageUtils.formatLangMessage(Message.MISSING_ARENAS, player)));
    }

}