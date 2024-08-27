package com.slyvr.xbedwars.game.phase;

import com.slyvr.xbedwars.api.game.Game;
import com.slyvr.xbedwars.api.game.phase.GamePhase;
import com.slyvr.xbedwars.api.team.GameTeam;
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