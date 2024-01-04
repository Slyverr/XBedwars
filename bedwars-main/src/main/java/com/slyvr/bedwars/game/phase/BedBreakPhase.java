package com.slyvr.bedwars.game.phase;

import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.game.phase.GamePhase;
import com.slyvr.bedwars.api.team.GameTeam;
import org.jetbrains.annotations.NotNull;


public final class BedBreakPhase extends GamePhase {


    public BedBreakPhase(int wait) {
        super("Bed Break", wait);
    }

    @Override
    public boolean apply(@NotNull Game game) {
        if (game == null)
            return false;

        for (GameTeam team : game.getGameTeams())
            game.breakTeamBed(team.getColor());

        return true;
    }

}