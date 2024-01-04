package com.slyvr.bedwars.player;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.bedwars.BedwarsItems;
import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.api.player.*;
import com.slyvr.bedwars.api.team.GameTeam;
import com.slyvr.bedwars.api.team.TeamColor;
import com.slyvr.bedwars.team.BedwarsTeamUtils;
import com.slyvr.bedwars.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class BedwarsPlayer implements GamePlayer {

    private final Set<Plugin> invincibility = new HashSet<>();
    private final Set<Plugin> invisibility = new HashSet<>();
    private final Set<Plugin> trap_safety = new HashSet<>();

    private final Game game;
    private final TeamColor color;
    private final GamePlayerInventory inventory;
    private final GamePlayerStatisticManager stats;
    private final GamePlayerRewardManager rewards;

    private Player player;
    private ArmorType armor;

    public BedwarsPlayer(@NotNull Game game, @NotNull Player player, @NotNull TeamColor color) {
        Preconditions.checkNotNull(game, "GamePlayer's game cannot be null!");
        Preconditions.checkNotNull(player, "GamePlayer's bukkit player cannot be null!");
        Preconditions.checkNotNull(color, "GamePlayer's team color cannot be null!");

        this.player = player;
        this.game = game;
        this.color = color;
        this.setArmorType(ArmorType.LEATHER);

        this.inventory = new BedwarsPlayerInventory();
        this.inventory.addItem(BedwarsItems.SWORD);

        this.stats = new BedwarsPlayerStatisticManager();
        this.rewards = new BedwarsPlayerRewardManager();
    }


    @Override
    public @NotNull Game getGame() {
        return game;
    }

    @Override
    public @NotNull GameTeam getTeam() {
        return game.getGameTeam(color);
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull TeamColor getTeamColor() {
        return color;
    }

    @Override
    public @NotNull ArmorType getArmorType() {
        return armor;
    }

    @Override
    public void setArmorType(@NotNull ArmorType type) {
        if (type != null)
            BedwarsTeamUtils.setPlayerArmor(player, (armor = type), color);
    }

    @Override
    public @NotNull GamePlayerInventory getInventory() {
        return inventory;
    }

    @Override
    public @NotNull GamePlayerStatisticManager getStatisticManager() {
        return stats;
    }

    @Override
    public @NotNull GamePlayerRewardManager getRewardManager() {
        return rewards;
    }

    @Override
    public void setInvisible(@NotNull Plugin plugin, boolean invisible) {
        if (plugin == null)
            return;

        if (!invisible) {
            if (invisibility.remove(plugin) && invisibility.isEmpty())
                Bedwars.getInstance().getUtils().showPlayerArmor(this);

            return;
        }

        if (invisibility.add(plugin) && invisibility.size() == 1)
            Bedwars.getInstance().getUtils().hidePlayerArmor(this);

        return;
    }

    @Override
    public boolean isInvisible() {
        return !invisibility.isEmpty();
    }

    @Override
    public void setInvincible(@NotNull Plugin plugin, boolean invincible) {
        if (plugin == null)
            return;

        if (invincible)
            this.invincibility.add(plugin);
        else
            this.invincibility.remove(plugin);
    }

    @Override
    public boolean isInvincible() {
        return !invincibility.isEmpty();
    }

    @Override
    public void setTrapSafe(@NotNull Plugin plugin, boolean safe) {
        if (plugin == null)
            return;

        if (safe)
            this.trap_safety.add(plugin);
        else
            this.trap_safety.remove(plugin);
    }

    @Override
    public boolean isTrapSafe() {
        return !trap_safety.isEmpty();
    }

    @Override
    public void sendMessage(@NotNull Message message, Object... args) {
        this.sendMessage(MessageUtils.formatLangMessage(message, player, args));
    }

    @Override
    public void sendMessage(@NotNull String message) {
        if (message != null)
            player.sendMessage(message);
    }

    @Override
    public void refresh() {
        Player refreshed = Bukkit.getPlayer(player.getUniqueId());
        if (refreshed == null)
            return;

        this.player = refreshed;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        BedwarsPlayer other = (BedwarsPlayer) obj;
        if (!game.equals(other.game))
            return false;

        if (!player.equals(other.player))
            return false;

        return inventory.equals(other.inventory) && stats.equals(other.stats) && armor == other.armor && color == other.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(game, player, inventory, stats, armor, color);
    }

    @Override
    public String toString() {
        return "BedwarsPlayer{Name='" + player.getName() + "'}";
    }

}