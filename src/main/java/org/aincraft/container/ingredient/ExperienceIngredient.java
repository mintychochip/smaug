package org.aincraft.container.ingredient;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;

public final class ExperienceIngredient implements Ingredient {

  private final int amount;

  ExperienceIngredient(int amount) {
    Preconditions.checkState(amount > 0);
    this.amount = amount;
  }

  @Override
  @Contract("null,_ -> false")
  public boolean isSubset(Player player, Inventory inventory) {
    if(player == null) {
      return false;
    }
    int totalExperience = player.getTotalExperience();
    return totalExperience >= amount;
  }

  @Override
  public void addIngredientToPlayer(Player player) {
    int totalExperience = player.getTotalExperience();
    player.setTotalExperience(amount + totalExperience);
  }
}
