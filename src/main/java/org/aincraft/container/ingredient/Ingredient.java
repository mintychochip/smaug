package org.aincraft.container.ingredient;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface Ingredient {
  boolean isSubset(Player player, Inventory inventory);
  void addIngredientToPlayer(Player player);
}
