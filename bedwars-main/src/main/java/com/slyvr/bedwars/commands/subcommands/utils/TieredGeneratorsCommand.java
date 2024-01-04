package com.slyvr.bedwars.commands.subcommands.utils;

import com.slyvr.bedwars.BedwarsHelp;
import com.slyvr.bedwars.api.arena.Arena;
import com.slyvr.bedwars.api.generator.Resource;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.utils.CommandUtils;
import com.slyvr.bedwars.utils.LocationUtils;
import com.slyvr.bedwars.utils.MessageUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public final class TieredGeneratorsCommand extends SubCommand {

    public TieredGeneratorsCommand() {
        super("tieredGens", "Shows all available tiered generators!", "bw.setup");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw tieredGens <arena> <resource>";
    }

    @Override
    public void perform(@NotNull Player player, @NotNull String[] args) {
        if (args.length < 3) {
            CommandUtils.sendUsage(this, player);
            return;
        }

        Arena arena = CommandUtils.getArena(args[1], player, false);
        if (arena == null)
            return;

        Resource resource = CommandUtils.getResource(args[2], player);
        if (resource == null)
            return;

        List<Location> locations = arena.getResourceGeneratorManager().getResourceGeneratorLocations(resource);
        if (locations.isEmpty()) {
            CommandUtils.sendArenaMessage(Message.ARENA_MISSING_GENERATORS, player, arena, resource.getColoredName());
            return;
        }

        player.sendMessage(BedwarsHelp.SEPARATOR);

        for (int i = 0; i < locations.size(); i++)
            this.sendLocationTextComponent(player, args[1], args[2], locations.get(i), i + 1);

        player.sendMessage(BedwarsHelp.SEPARATOR);
    }

    private void sendLocationTextComponent(@NotNull Player player, @NotNull String arena, @NotNull String resource, @NotNull Location loc, int index) {
        TextComponent index_component = new TextComponent(ChatColor.GOLD + String.valueOf(index) + ". ");

        TextComponent location_component = new TextComponent(ChatColor.GRAY + LocationUtils.serialize(loc, false));
        location_component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getTeleportCommand(loc)));
        location_component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, getTeleportText(player)));

        TextComponent remover_component = new TextComponent(ChatColor.RED + " ✘");
        remover_component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getRemoveCommand(arena, resource, index)));
        remover_component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, getRemoveText(player)));

        player.spigot().sendMessage(new TextComponent[]{index_component, location_component, remover_component});
    }

    @NotNull
    private TextComponent[] getTeleportText(@NotNull Player player) {
        TextComponent text = new TextComponent(MessageUtils.formatLangMessage(Message.INTERACTION_CLICK_TO_TELEPORT, player));
        return new TextComponent[]{text};
    }

    @NotNull
    private TextComponent[] getRemoveText(@NotNull Player player) {
        TextComponent text = new TextComponent(MessageUtils.formatLangMessage(Message.INTERACTION_CLICK_TO_REMOVE, player));
        return new TextComponent[]{text};
    }

    @NotNull
    private String getTeleportCommand(@NotNull Location loc) {
        return "/tp " + loc.getX() + ' ' + loc.getY() + ' ' + loc.getZ();
    }

    @NotNull
    private String getRemoveCommand(@NotNull String arena, @NotNull String resource, int index) {
        return "/bw removeTieredGen " + arena + ' ' + resource + ' ' + index;
    }

}