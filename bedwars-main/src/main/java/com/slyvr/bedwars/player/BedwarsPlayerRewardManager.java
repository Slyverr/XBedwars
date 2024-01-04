package com.slyvr.bedwars.player;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.lang.Language;
import com.slyvr.bedwars.api.player.GamePlayerRewardManager;
import com.slyvr.bedwars.api.reward.GameRewardReason;
import com.slyvr.bedwars.api.reward.GameRewardType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public final class BedwarsPlayerRewardManager implements GamePlayerRewardManager {

    private final Map<GameRewardType, GameRewardStorage> rewards = new HashMap<>();

    public BedwarsPlayerRewardManager() {
    }

    @NotNull
    @Override
    public Set<GameRewardType> getRewards() {
        return new HashSet<>(rewards.keySet());
    }

    @Override
    public @NotNull Map<GameRewardReason, Integer> getIncrementHistory(@NotNull GameRewardType type) {
        GameRewardStorage storage = rewards.get(type);
        return storage != null ? new HashMap<>(storage.increments) : new HashMap<>(0);
    }

    @Override
    public @NotNull Map<GameRewardReason, Integer> getDecrementHistory(@NotNull GameRewardType type) {
        GameRewardStorage storage = rewards.get(type);
        return storage != null ? new HashMap<>(storage.decrements) : new HashMap<>(0);
    }

    @Override
    public void increment(@NotNull GameRewardType type, @NotNull GameRewardReason reason, int amount) {
        if (type == null || reason == null || amount < 1)
            return;

        GameRewardStorage storage = rewards.computeIfAbsent(type, k -> new GameRewardStorage());
        storage.increment(reason, amount);
    }

    @Override
    public void decrement(@NotNull GameRewardType type, @NotNull GameRewardReason reason, int amount) {
        if (type == null || reason == null || amount < 1)
            return;

        GameRewardStorage storage = rewards.get(type);
        if (storage != null)
            storage.decrement(reason, amount);
    }

    @Override
    public int getTotalAmount(@NotNull GameRewardType type) {
        GameRewardStorage storage = rewards.get(type);
        return storage != null ? storage.total : 0;
    }

    @Override
    public int getTotalAmount() {
        int result = 0;

        for (GameRewardStorage reward : rewards.values())
            result += reward.total;

        return result;
    }

    @Override
    public @NotNull TextComponent getRewardHistoryText(@NotNull GameRewardType type, @NotNull Language lang) {
        Preconditions.checkNotNull(type, "Cannot get the text for a null reward!");
        Preconditions.checkNotNull(lang, "Cannot format the text with a null language!");

        GameRewardStorage storage = rewards.get(type);
        if (storage == null || storage.total == 0)
            return new TextComponent(type.getColor() + "0 " + type.getName(lang));

        TextComponent result = new TextComponent(type.getColor().toString() + storage.total + ' ' + type.getName(lang));
        if (storage.increments.isEmpty() && storage.decrements.isEmpty())
            return result;

        result.setHoverEvent(getHoverEvent(type, storage));
        return result;
    }

    @Override
    public @NotNull String getRewardText(@NotNull GameRewardType type, @NotNull Language lang) {
        Preconditions.checkNotNull(type, "Cannot get the text for a null reward!");
        Preconditions.checkNotNull(lang, "Cannot format the text with a null language!");

        GameRewardStorage storage = rewards.get(type);
        return getRewardText(type, lang, storage != null ? storage.total : 0);
    }

    private String getRewardText(@NotNull GameRewardType type, @NotNull Language lang, int amount) {
        return type.getColor().toString() + amount + ' ' + type.getName(lang);
    }

    private HoverEvent getHoverEvent(@NotNull GameRewardType type, @NotNull BedwarsPlayerRewardManager.GameRewardStorage storage) {
        StringBuilder builder = new StringBuilder();

        for (Entry<GameRewardReason, Integer> entry : storage.increments.entrySet())
            builder.append(getIncrementText(type, entry.getKey(), entry.getValue())).append('\n');

        for (Entry<GameRewardReason, Integer> entry : storage.decrements.entrySet())
            builder.append(getDecrementText(type, entry.getKey(), entry.getValue())).append('\n');

        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(builder.toString())});
    }

    private String getIncrementText(@NotNull GameRewardType type, @NotNull GameRewardReason reason, int amount) {
        return String.valueOf(type.getColor()) + '+' + amount + ' ' + reason.getReason();
    }

    private String getDecrementText(@NotNull GameRewardType type, @NotNull GameRewardReason reason, int amount) {
        return String.valueOf(type.getColor()) + '-' + amount + ' ' + reason.getReason();
    }


    private static final class GameRewardStorage {

        private final Map<GameRewardReason, Integer> increments = new HashMap<>();
        private final Map<GameRewardReason, Integer> decrements = new HashMap<>();

        private int total;

        public GameRewardStorage() {
        }

        public void increment(@NotNull GameRewardReason reason, int amount) {
            this.increments.compute(reason, (key, value) -> value != null ? value + amount : amount);
            this.total += amount;
        }

        public void decrement(@NotNull GameRewardReason reason, int amount) {
            this.decrements.compute(reason, (key, value) -> value != null ? value + amount : amount);

            this.total -= amount;
            if (total < 0)
                this.total = 0;
        }

    }

}
