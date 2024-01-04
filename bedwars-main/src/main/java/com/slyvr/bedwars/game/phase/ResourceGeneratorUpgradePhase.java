package com.slyvr.bedwars.game.phase;

import com.google.common.base.Preconditions;
import com.slyvr.bedwars.api.game.Game;
import com.slyvr.bedwars.api.game.phase.GamePhase;
import com.slyvr.bedwars.api.generator.Resource;
import com.slyvr.bedwars.api.generator.tiered.TieredResourceGenerator;
import com.slyvr.bedwars.api.lang.Message;
import com.slyvr.bedwars.utils.MessageUtils;
import org.jetbrains.annotations.NotNull;


public final class ResourceGeneratorUpgradePhase extends GamePhase {

    private final Resource resource;
    private final String tier_text;
    private final int tier;


    public ResourceGeneratorUpgradePhase(@NotNull Resource resource, @NotNull String tierText, int wait, int tier) {
        super(resource.getName() + ' ' + tierText, wait);

        Preconditions.checkNotNull(resource, "Resource to upgrade cannot be null!");
        Preconditions.checkNotNull(tierText, "Upgrade's tier text cannot be null!");
        Preconditions.checkArgument(tier > 0, "Upgrade's tier must be positive!");

        this.resource = resource;
        this.tier_text = tierText;

        this.tier = tier;
    }

    @Override
    public boolean apply(@NotNull Game game) {
        if (game == null)
            return false;

        for (TieredResourceGenerator gen : game.getResourceGenerators(resource))
            gen.setCurrentTier(tier);

        game.forEach(player -> {
            MessageUtils.sendLangMessage(Message.GAME_RESOURCE_GENERATOR_UPGRADE, player.getPlayer(), resource.getColoredName(), tier_text);
        });
        return true;
    }

}