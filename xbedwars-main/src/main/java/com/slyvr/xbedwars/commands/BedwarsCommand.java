package com.slyvr.xbedwars.commands;

import com.slyvr.xbedwars.XBedwarsHelp;
import com.slyvr.xbedwars.api.lang.Message;
import com.slyvr.xbedwars.commands.subcommands.SubCommand;
import com.slyvr.xbedwars.commands.subcommands.arena.CreateCommand;
import com.slyvr.xbedwars.commands.subcommands.arena.DisableCommand;
import com.slyvr.xbedwars.commands.subcommands.arena.EnableCommand;
import com.slyvr.xbedwars.commands.subcommands.arena.RemoveCommand;
import com.slyvr.xbedwars.commands.subcommands.arena.RemoveTieredGeneratorCommand;
import com.slyvr.xbedwars.commands.subcommands.arena.SaveCommand;
import com.slyvr.xbedwars.commands.subcommands.arena.SetBossSpawnCommand;
import com.slyvr.xbedwars.commands.subcommands.arena.SetMapNameCommand;
import com.slyvr.xbedwars.commands.subcommands.arena.SetModeCommand;
import com.slyvr.xbedwars.commands.subcommands.arena.SetPhasePresetCommand;
import com.slyvr.xbedwars.commands.subcommands.arena.SetRegionCommand;
import com.slyvr.xbedwars.commands.subcommands.arena.SetSpectatorSpawnCommand;
import com.slyvr.xbedwars.commands.subcommands.arena.SetTeamGeneratorPresetCommand;
import com.slyvr.xbedwars.commands.subcommands.arena.SetTieredGeneratorCommand;
import com.slyvr.xbedwars.commands.subcommands.arena.SetTieredGeneratorPresetCommand;
import com.slyvr.xbedwars.commands.subcommands.arena.SetTimeCommand;
import com.slyvr.xbedwars.commands.subcommands.arena.SetWaitingRoomRegionCommand;
import com.slyvr.xbedwars.commands.subcommands.arena.SetWaitingRoomSpawnCommand;
import com.slyvr.xbedwars.commands.subcommands.lobby.LobbyCommand;
import com.slyvr.xbedwars.commands.subcommands.lobby.RemoveLobbyCommand;
import com.slyvr.xbedwars.commands.subcommands.lobby.SetLobbyCommand;
import com.slyvr.xbedwars.commands.subcommands.team.SetTeamBedCommand;
import com.slyvr.xbedwars.commands.subcommands.team.SetTeamChestCommand;
import com.slyvr.xbedwars.commands.subcommands.team.SetTeamGeneratorCommand;
import com.slyvr.xbedwars.commands.subcommands.team.SetTeamShopCommand;
import com.slyvr.xbedwars.commands.subcommands.team.SetTeamSpawnCommand;
import com.slyvr.xbedwars.commands.subcommands.team.SetTeamUpgradeCommand;
import com.slyvr.xbedwars.commands.subcommands.user.LangCommand;
import com.slyvr.xbedwars.commands.subcommands.user.SetLevelCommand;
import com.slyvr.xbedwars.commands.subcommands.user.SetProgressCommand;
import com.slyvr.xbedwars.commands.subcommands.user.StatsCommand;
import com.slyvr.xbedwars.commands.subcommands.user.StatsViewCommand;
import com.slyvr.xbedwars.commands.subcommands.utils.ArenasCommand;
import com.slyvr.xbedwars.commands.subcommands.utils.LangsCommand;
import com.slyvr.xbedwars.commands.subcommands.utils.LobbiesCommand;
import com.slyvr.xbedwars.commands.subcommands.utils.MapSelectorCommand;
import com.slyvr.xbedwars.commands.subcommands.utils.ModesCommand;
import com.slyvr.xbedwars.commands.subcommands.utils.PresetsCommand;
import com.slyvr.xbedwars.commands.subcommands.utils.ResourcesCommand;
import com.slyvr.xbedwars.commands.subcommands.utils.StateCommand;
import com.slyvr.xbedwars.commands.subcommands.utils.StopCommand;
import com.slyvr.xbedwars.commands.subcommands.utils.TeamsCommand;
import com.slyvr.xbedwars.commands.subcommands.utils.TieredGeneratorsCommand;
import com.slyvr.xbedwars.utils.CommandUtils;
import com.slyvr.xbedwars.utils.MessageUtils;
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
            XBedwarsHelp.send(player);
            return;
        }

        SubCommand sub = SUB_COMMANDS.get(args[1].toLowerCase());
        if (sub != null) {
            XBedwarsHelp.send(player, sub);
            return;
        }

        int page = NumberConversions.toInt(args[1]);
        if (!XBedwarsHelp.isValidPage(page)) {
            CommandUtils.sendMessage(Message.INVALID_NUMBER_PAGE, player);
            return;
        }

        XBedwarsHelp.send(player, page);
    }

    @NotNull
    public static List<SubCommand> getSubCommandsList() {
        return new ArrayList<>(SUB_COMMANDS.values());
    }

    private static void register(@NotNull SubCommand command) {
        BedwarsCommand.SUB_COMMANDS.put(command.getName().toLowerCase(), command);
    }

}