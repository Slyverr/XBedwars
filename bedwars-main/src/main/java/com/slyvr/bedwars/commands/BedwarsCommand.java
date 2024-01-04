package com.slyvr.bedwars.commands;

import com.slyvr.bedwars.BedwarsHelp;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.commands.subcommands.arena.*;
import com.slyvr.bedwars.commands.subcommands.lobby.LobbyCommand;
import com.slyvr.bedwars.commands.subcommands.lobby.RemoveLobbyCommand;
import com.slyvr.bedwars.commands.subcommands.lobby.SetLobbyCommand;
import com.slyvr.bedwars.commands.subcommands.team.*;
import com.slyvr.bedwars.commands.subcommands.user.*;
import com.slyvr.bedwars.commands.subcommands.utils.*;
import com.slyvr.bedwars.utils.CommandUtils;
import com.slyvr.bedwars.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public final class BedwarsCommand implements CommandExecutor {

    private static final Map<String, SubCommand> SUB_COMMANDS = new LinkedHashMap<>();

    static {
        BedwarsCommand.register(new CreateCommand());
        BedwarsCommand.register(new RemoveCommand());
        BedwarsCommand.register(new EnableCommand());
        BedwarsCommand.register(new DisableCommand());
        BedwarsCommand.register(new SaveCommand());

        BedwarsCommand.register(new SetMapNameCommand());
        BedwarsCommand.register(new SetTimeCommand());
        BedwarsCommand.register(new SetModeCommand());

        BedwarsCommand.register(new SetBossSpawnCommand());
        BedwarsCommand.register(new SetSpectatorSpawnCommand());
        BedwarsCommand.register(new SetWaitingRoomSpawnCommand());

        BedwarsCommand.register(new SetWaitingRoomRegionCommand());
        BedwarsCommand.register(new SetRegionCommand());

        BedwarsCommand.register(new SetTieredGeneratorPresetCommand());
        BedwarsCommand.register(new SetTieredGeneratorCommand());
        BedwarsCommand.register(new RemoveTieredGeneratorCommand());

        BedwarsCommand.register(new SetTeamGeneratorPresetCommand());
        BedwarsCommand.register(new SetTeamGeneratorCommand());

        BedwarsCommand.register(new SetTeamBedCommand());
        BedwarsCommand.register(new SetTeamSpawnCommand());
        BedwarsCommand.register(new SetTeamShopCommand());
        BedwarsCommand.register(new SetTeamUpgradeCommand());
        BedwarsCommand.register(new SetTeamGeneratorCommand());
        BedwarsCommand.register(new SetTeamChestCommand());

        BedwarsCommand.register(new SetLobbyCommand());
        BedwarsCommand.register(new RemoveLobbyCommand());
        BedwarsCommand.register(new SetPhasePresetCommand());

        BedwarsCommand.register(new SetLevelCommand());
        BedwarsCommand.register(new SetProgressCommand());
        BedwarsCommand.register(new StatsViewCommand());
        BedwarsCommand.register(new StatsCommand());
        BedwarsCommand.register(new LangCommand());

        BedwarsCommand.register(new TieredGeneratorsCommand());
        BedwarsCommand.register(new MapSelectorCommand());
        BedwarsCommand.register(new ResourcesCommand());
        BedwarsCommand.register(new PresetsCommand());
        BedwarsCommand.register(new LobbiesCommand());
        BedwarsCommand.register(new ArenasCommand());
        BedwarsCommand.register(new ModesCommand());
        BedwarsCommand.register(new TeamsCommand());
        BedwarsCommand.register(new LangsCommand());
        BedwarsCommand.register(new LobbyCommand());
        BedwarsCommand.register(new StateCommand());
        BedwarsCommand.register(new StopCommand());
    }

    @NotNull
    public static List<SubCommand> getSubCommandsList() {
        return new ArrayList<>(SUB_COMMANDS.values());
    }

    private static void register(@NotNull SubCommand command) {
        BedwarsCommand.SUB_COMMANDS.put(command.getName().toLowerCase(), command);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players who can execute this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            if (!player.hasPermission("bw.admin") && !player.hasPermission("bw.setup")) {
                MessageUtils.sendLangMessage(Message.COMMAND_PERMISSION, player);
                return true;
            }

            player.sendMessage(CommandUtils.info("/bw help/? <page>"));
            player.sendMessage(CommandUtils.info("/bw help/? <command>"));
            return true;
        }

        String first = args[0].toLowerCase();
        if (first.equals("help") || first.equals("?")) {
            if (!player.hasPermission("bw.admin") && !player.hasPermission("bw.setup")) {
                MessageUtils.sendLangMessage(Message.COMMAND_PERMISSION, player);
                return true;
            }

            this.sendHelp(player, args);
            return true;
        }

        SubCommand sub = BedwarsCommand.SUB_COMMANDS.get(first);
        if (sub == null)
            return true;

        if (!player.hasPermission("bw.admin") && !player.hasPermission(sub.getPermission())) {
            MessageUtils.sendLangMessage(Message.COMMAND_PERMISSION, player);
            return true;
        }

        sub.perform(player, args);
        return true;
    }

    private void sendHelp(@NotNull Player player, @NotNull String[] args) {
        if (args.length == 1) {
            BedwarsHelp.send(player);
            return;
        }

        SubCommand sub = SUB_COMMANDS.get(args[1].toLowerCase());
        if (sub != null) {
            BedwarsHelp.send(player, sub);
            return;
        }

        int page = NumberConversions.toInt(args[1]);
        if (!BedwarsHelp.isValidPage(page)) {
            CommandUtils.sendMessage(Message.INVALID_NUMBER_PAGE, player);
            return;
        }

        BedwarsHelp.send(player, page);
    }

}