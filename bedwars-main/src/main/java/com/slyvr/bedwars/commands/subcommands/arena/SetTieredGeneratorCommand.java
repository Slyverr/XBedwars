package com.slyvr.bedwars.commands.subcommands.arena;

import com.slyvr.bedwars.api.arena.Arena;
import com.slyvr.bedwars.api.generator.Resource;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class SetTieredGeneratorCommand extends SubCommand {

    public SetTieredGeneratorCommand() {
        super("setTieredGen", "Adds a tiered generator to the arena!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw setTieredGen <arena> <resource>";
    }

    @Override
    public void perform(@NotNull Player player, String[] args) {
        if (args.length < 3) {
            CommandUtils.sendUsage(this, player);
            return;
        }

        Arena arena = CommandUtils.getArena(args[1], player, true);
        if (arena == null)
            return;

        Resource resource = CommandUtils.getResource(args[2], player);
        if (resource == null)
            return;

        arena.getResourceGeneratorManager().addResourceGenerator(resource, player.getLocation());
        CommandUtils.sendArenaMessage(Message.ARENA_MODIFICATION_RESOURCE_GENERATOR_ADDED, player, arena, resource.getColoredName());
    }

}
