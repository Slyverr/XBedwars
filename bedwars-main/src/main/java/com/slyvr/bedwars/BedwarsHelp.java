package com.slyvr.bedwars;

import com.slyvr.bedwars.commands.BedwarsCommand;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.utils.ChatUtils;
import com.slyvr.chat.ChatTextSection;
import com.slyvr.chat.utils.ChatTextUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public final class BedwarsHelp {

    public static final String SEPARATOR = ChatUtils.format("&a&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

    private static final ChatTextSection[] PAGES;

    static {
        List<SubCommand> list = BedwarsCommand.getSubCommandsList();

        int length = (int) Math.ceil(list.size() / 8.0);
        PAGES = new ChatTextSection[length];

        for (int i = 0; i < length; i++) {
            ChatTextSection page = new ChatTextSection();
            page.append(getPageHeader(i + 1, length));

            for (int j = 0; j < 8; j++) {
                int index = i * 8 + j;

                if (index < list.size())
                    page.append(buildCommand(list.get(index)));
            }

            page.append(SEPARATOR);
            PAGES[i] = page;
        }

    }

    private BedwarsHelp() {
    }

    public static void send(@NotNull Player player, @NotNull SubCommand sub) {
        player.sendMessage(SEPARATOR);
        player.sendMessage(ChatTextUtils.alignToCenter(ChatUtils.bold(sub.getName())));
        player.sendMessage("");
        player.sendMessage(ChatUtils.format("&bDescription: &7" + sub.getDescription()));
        player.sendMessage(ChatUtils.format("&bPermission: &7" + sub.getPermission()));
        player.sendMessage(ChatUtils.format("&bUsage: &7" + sub.getUsage()));
        player.sendMessage("");
        player.sendMessage(SEPARATOR);
    }

    public static void send(@NotNull Player player, int page) {
        if (page >= 1 && page <= PAGES.length)
            PAGES[page - 1].sendSection(player);
    }

    public static void send(@NotNull Player player) {
        BedwarsHelp.send(player, 1);
    }

    public static boolean isValidPage(int page) {
        return page >= 1 && page <= PAGES.length;
    }

    private static String getPageHeader(int page, int max) {
        String border = ChatUtils.format("&a&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        return border + ChatUtils.format(" &b&lBed Wars Help &7(&b" + page + "&7/&b" + max + "&7) ") + border;
    }

    private static String buildCommand(@NotNull SubCommand sub) {
        return ChatColor.YELLOW + "/bw " + sub.getName() + ": " + ChatColor.GRAY + sub.getDescription();
    }

}