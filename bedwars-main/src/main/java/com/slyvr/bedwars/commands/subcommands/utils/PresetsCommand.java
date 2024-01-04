package com.slyvr.bedwars.commands.subcommands.utils;

import com.slyvr.bedwars.api.game.phase.GamePhasePreset;
import com.slyvr.bedwars.api.generator.team.TeamResourceGeneratorPreset;
import com.slyvr.bedwars.api.generator.tiered.TieredResourceGeneratorPreset;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.utils.CommandUtils;
import com.slyvr.bedwars.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;


public final class PresetsCommand extends SubCommand {

    public PresetsCommand() {
        super("presets", "Shows all available generators presets!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw presets <tiered/teams/phases>";
    }

    @Override
    public void perform(@NotNull Player player, @NotNull String[] args) {
        if (args.length < 2) {
            CommandUtils.sendUsage(this, player);
            return;
        }

        switch (args[1].toLowerCase()) {
            case "tiered":
                this.sendData(TieredResourceGeneratorPreset.values(), TieredResourceGeneratorPreset::getName, player);
                break;
            case "teams":
                this.sendData(TeamResourceGeneratorPreset.values(), TeamResourceGeneratorPreset::getName, player);
                break;
            case "phases":
                this.sendData(GamePhasePreset.values(), GamePhasePreset::getName, player);
                break;
        }

    }

    private <T> void sendData(@NotNull T[] presets, @NotNull Function<T, String> function, @NotNull Player player) {
        player.sendMessage(CommandUtils.format(presets, function, MessageUtils.formatLangMessage(Message.MISSING_PRESETS, player)));
    }

}