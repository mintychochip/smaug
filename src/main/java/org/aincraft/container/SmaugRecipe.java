package org.aincraft.container;

import java.util.List;
import org.aincraft.container.ingredient.Ingredient;
import org.aincraft.container.item.KeyedItem;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public record SmaugRecipe(KeyedItem output, List<Ingredient> ingredients,
                          NamespacedKey recipeKey, NamespacedKey stationKey) implements Keyed {

  public boolean isSubset(Player player, Inventory inventory) {
    for (Ingredient ingredient : ingredients) {
      if (!ingredient.isSubset(player, inventory)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public @NotNull NamespacedKey getKey() {
    return recipeKey;
  }

  public List<Ingredient> getIngredients() {
    return ingredients;
  }

  public KeyedItem getOutput() {
    return output;
  }
}
