package com.slyvr.bedwars.commands.subcommands.user;

import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.api.user.OfflineUser;
import com.slyvr.bedwars.api.user.User;
import com.slyvr.bedwars.api.user.level.UserLevel;
import com.slyvr.bedwars.commands.subcommands.SubCommand;
import com.slyvr.bedwars.manager.BedwarsUsersManager;
import com.slyvr.bedwars.utils.CommandUtils;
import com.slyvr.bedwars.utils.NumberUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;


public final class SetProgressCommand extends SubCommand {

    public SetProgressCommand() {
        super("setProgress", "Sets the progress of a user!", "bw.commands.progress");
    }

    @Override
    public @NotNull String getUsage() {
        return "/bw setProgress <progress> <player>";
    }

    @Override
    public void perform(@NotNull Player player, @NotNull String[] args) {
        if (args.length < 2) {
            CommandUtils.sendUsage(this, player);
            return;
        }

        int exp = NumberConversions.toInt(args[1]);
        if (exp < 0) {
            CommandUtils.sendMessage(Message.INVALID_NUMBER_PROGRESS, player);
            return;
        }

        if (args.length == 2) {
            this.setProgress(player, exp);
            return;
        }

        OfflinePlayer target = CommandUtils.getOfflinePlayer(args[2], player);
        if (target == null)
            return;

        this.setProgress(target, exp);
    }

    private void setProgress(@NotNull OfflinePlayer target, int exp) {
        OfflineUser user = BedwarsUsersManager.getInstance().getOfflineUser(target);

        UserLevel current = user.getLevel();
        current.setProgress(exp);

        user.setLevel(current);
        if (!target.isOnline())
            return;

        User online_user = (User) user;
        online_user.getScoreboard().update(online_user);

        Player player = target.getPlayer();
        player.setExp(current.getProgressPercentage());

        CommandUtils.sendMessage(Message.USER_MODIFICATION_LEVEL_PROGRESS, player, NumberUtils.formatWithComma(current.getProgress()));
    }

}