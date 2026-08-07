package org.aincraft.api.refining;

import org.aincraft.container.SmaugRecipe;
import org.bukkit.entity.Player;

public interface ProfessionGateway {

  ProfessionState state(Player player, SmaugRecipe recipe);

  void awardXp(Player player, SmaugRecipe recipe, int amount);
}
