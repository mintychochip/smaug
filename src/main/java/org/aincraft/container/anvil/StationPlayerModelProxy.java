package org.aincraft.container.anvil;

import org.aincraft.database.model.RecipeProgress;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationInventory;
import org.bukkit.entity.Player;

public record StationPlayerModelProxy(Player player, StationInventory inventory,
                                      RecipeProgress recipeProgress, Station station) {

  @Override
  public int hashCode() {
    return player.hashCode() + inventory.hashCode();
  }
}
