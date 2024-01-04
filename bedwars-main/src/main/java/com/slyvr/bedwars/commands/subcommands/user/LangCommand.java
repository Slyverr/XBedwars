package com.slyvr.bedwars.commands.subcommands.user;

import com.slyvr.bedwars.api.lang.Language;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.api.user.OfflineUser;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.manager.BedwarsUsersManager;
import com.slyvr.bedwars.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class LangCommand extends SubCommand {

    public LangCommand() {
        super("lang", "Sets the language of the user!", "bw.commands.lang");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw lang <language>";
    }

    @Override
    public void perform(@NotNull Player player, @NotNull String[] args) {
        if (args.length < 2) {
            CommandUtils.sendUsage(this, player);
            return;
        }

        Language lang = CommandUtils.getLanguage(args[1], player);
        if (lang == null)
            return;

        OfflineUser user = BedwarsUsersManager.getInstance().getOfflineUser(player);
        if (user.getLanguage() == lang) {
            CommandUtils.sendMessage(Message.USER_MODIFICATION_LANGUAGE_RETAINED, player, lang.getFullName());
            return;
        }

        user.setLanguage(lang);
        CommandUtils.sendMessage(Message.USER_MODIFICATION_LANGUAGE_CHANGED, player, lang.getFullName());
    }

}