package com.slyvr.bedwars.commands.subcommands.arena;

import com.slyvr.bedwars.api.arena.Arena;
import com.slyvr.bedwars.api.generator.team.TeamResourceGeneratorPreset;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class SetTeamGeneratorPresetCommand extends SubCommand {

    public SetTeamGeneratorPresetCommand() {
        super("setTeamGenPreset", "Sets the preset of teams generator in an arena!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw setTeamGenPreset <arena> <preset>";
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

        TeamResourceGeneratorPreset preset = CommandUtils.getTeamGeneratorPreset(args[2], player);
        if (preset == null)
            return;

        arena.getResourceGeneratorManager().setTeamResourceGeneratorPreset(preset);
        CommandUtils.sendArenaMessage(Message.ARENA_MODIFICATION_PRESETS_TEAM_RESOURCE_GENERATOR, player, arena, args[2]);
    }

}
