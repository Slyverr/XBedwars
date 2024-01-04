package com.slyvr.bedwars.commands.subcommands.user;

import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.api.user.OfflineUser;
import com.slyvr.bedwars.api.user.User;
import com.slyvr.bedwars.api.user.level.UserLevel;
import com.slyvr.bedwars.api.user.level.UserPrestige;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.manager.BedwarsUsersManager;
import com.slyvr.bedwars.utils.CommandUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;


public final class SetLevelCommand extends SubCommand {

    public SetLevelCommand() {
        super("setLevel", "Sets the level of a user!", "bw.commands.level");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw setLevel <level> <player>";
    }

    @Override
    public void perform(@NotNull Player player, @NotNull String[] args) {
        if (args.length < 2) {
            CommandUtils.sendUsage(this, player);
            return;
        }

        int level = NumberConversions.toInt(args[1]);
        if (level < 1) {
            CommandUtils.sendMessage(Message.INVALID_NUMBER_LEVEL, player);
            return;
        }

        if (args.length == 2) {
            this.setLevel(player, level);
            return;
        }

        OfflinePlayer target = CommandUtils.getOfflinePlayer(args[2], player);
        if (target == null)
            return;

        this.setLevel(target, level);
    }

    private void setLevel(@NotNull OfflinePlayer target, int level) {
        OfflineUser offline_user = BedwarsUsersManager.getInstance().getOfflineUser(target);

        UserLevel current = offline_user.getLevel();
        current.setLevel(level, true);

        offline_user.setLevel(current);
        offline_user.setPrestige(UserPrestige.getByLevel(level, UserPrestige.DEFAULT));

        if (!target.isOnline())
            return;

        User user = (User) offline_user;
        user.getScoreboard().update(user);

        Player player = target.getPlayer();

        player.setLevel(current.getLevel());
        CommandUtils.sendMessage(Message.USER_MODIFICATION_LEVEL, player, user.getPrestige().formatToChat(current));
    }

}