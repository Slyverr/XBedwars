package com.slyvr.xbedwars.commands.subcommands.arena;

import com.slyvr.xbedwars.api.arena.Arena;
import com.slyvr.xbedwars.api.generator.Resource;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;


public final class RemoveTieredGeneratorCommand extends SubCommand {

    public RemoveTieredGeneratorCommand() {
        super("removeTieredGen", "Removes a tiered generator from the arena!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw setResourceGen <arena> <resource> <index>";
    }

    @Override
    public void perform(@NotNull Player player, String[] args) {
        if (args.length < 4) {
            CommandUtils.sendUsage(this, player);
            return;
        }

        Arena arena = CommandUtils.getArena(args[1], player, true);
        if (arena == null)
            return;

        Resource resource = CommandUtils.getResource(args[2], player);
        if (resource == null)
            return;

        int index = NumberConversions.toInt(args[3]);
        if (index < 0) {
            CommandUtils.sendArenaMessage(Message.INVALID_NUMBER_INDEX, player, arena);
            return;
        }

        if (arena.getResourceGeneratorManager().removeResourceGenerator(resource, index - 1))
            CommandUtils.sendArenaMessage(Message.ARENA_MODIFICATION_RESOURCE_GENERATOR_REMOVED, player, arena, resource.getColoredName());
        else
            CommandUtils.sendArenaMessage(Message.ARENA_MODIFICATION_RESOURCE_GENERATOR_RETAINED, player, arena);
    }

}
