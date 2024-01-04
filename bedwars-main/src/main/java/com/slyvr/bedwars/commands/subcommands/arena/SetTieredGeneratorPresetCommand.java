package com.slyvr.bedwars.commands.subcommands.arena;

import com.slyvr.bedwars.api.arena.Arena;
import com.slyvr.bedwars.api.generator.Resource;
import com.slyvr.bedwars.api.generator.tiered.TieredResourceGeneratorPreset;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class SetTieredGeneratorPresetCommand extends SubCommand {

    public SetTieredGeneratorPresetCommand() {
        super("setTieredGenPreset", "Sets the preset of teams generator in an arena!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw setTieredGenPreset <arena> <resource> <preset>";
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

        TieredResourceGeneratorPreset preset = CommandUtils.getTieredGeneratorPreset(args[3], player);
        if (preset == null)
            return;

        arena.getResourceGeneratorManager().setTieredResourceGeneratorPreset(resource, preset);
        CommandUtils.sendArenaMessage(Message.ARENA_MODIFICATION_PRESETS_TIERED_RESOURCE_GENERATOR, player, arena, resource.getColoredName(), args[3]);
    }

}
