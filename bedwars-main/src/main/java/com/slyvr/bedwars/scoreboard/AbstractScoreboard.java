package com.slyvr.bedwars.scoreboard;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.scoreboard.Scoreboard;
import com.slyvr.bedwars.utils.ChatUtils;
import com.slyvr.scoreboard.ScoreboardTitle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public abstract class AbstractScoreboard implements Scoreboard {

    protected final String[] lines = new String[15];
    protected final ScoreboardTitle title;

    public AbstractScoreboard(@NotNull ScoreboardTitle title) {
        Preconditions.checkNotNull(title, "Scoreboard's title cannot be null!");

        this.title = title;
    }

    @Override
    public @NotNull ScoreboardTitle getTitle() {
        return title;
    }

    @Override
    public @NotNull List<String> getLines() {
        List<String> result = new ArrayList<>(15);

        Collections.addAll(result, lines);
        return result;
    }

    @Override
    public void setLines(@NotNull List<String> lines) {
        if (lines == null)
            return;

        int size = lines.size() - 1;
        for (int i = 0; i <= size && i < 15; i++)
            this.lines[i] = ChatUtils.format(lines.get(size - i));
    }

    @Override
    public @Nullable String getText(@Range(from = 1, to = 15) int line) {
        return isValidLine(line) ? lines[line - 1] : null;
    }

    @Override
    public void setText(@Range(from = 1, to = 15) int line, @Nullable String text) {
        if (text != null && isValidLine(line))
            this.lines[line - 1] = ChatUtils.format(text);
    }

    private boolean isValidLine(int line) {
        return line >= 1 && line <= 15;
    }

}