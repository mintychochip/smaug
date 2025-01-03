package org.aincraft.container;

import org.aincraft.container.ingredient.IngredientList;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRecipeEvaluator {

  enum Status {
    SUCCESS,
    PERMISSION_FAILURE,
    INSUFFICIENT_RESOURCES
  }

  RecipeResult test(SmaugRecipe recipe, Player player, Inventory inventory);

  interface RecipeResult {

    @NotNull
    Status getStatus();

    @Nullable
    IngredientList getMissing();

    @Nullable
    String getError();
  }
}
