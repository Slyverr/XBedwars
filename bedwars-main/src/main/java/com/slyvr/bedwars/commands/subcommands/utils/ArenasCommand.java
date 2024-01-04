package com.slyvr.bedwars.commands.subcommands.utils;

import com.slyvr.bedwars.api.arena.Arena;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.manager.BedwarsArenasManager;
import com.slyvr.bedwars.utils.CommandUtils;
import com.slyvr.bedwars.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;


public final class ArenasCommand extends SubCommand {


    public ArenasCommand() {
        super("arenas", "Shows all available bedwars arenas!", "bw.setup");
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