package com.slyvr.bedwars.commands.subcommands.arena;

import com.slyvr.bedwars.api.arena.Arena;
import com.slyvr.bedwars.api.game.phase.GamePhasePreset;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class SetPhasePresetCommand extends SubCommand {

    public SetPhasePresetCommand() {
        super("setPhasesPreset", "Sets the preset of phases for an arena!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw setPhasesPreset <arena> <preset>";
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

        GamePhasePreset preset = CommandUtils.getGamePhasePreset(args[2], player);
        if (preset == null)
            return;

        arena.setPhasesPreset(preset);
        CommandUtils.sendArenaMessage(Message.ARENA_MODIFICATION_PRESETS_PHASES, player, arena, args[2]);
    }

}
