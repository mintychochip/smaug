package org.aincraft.container;

import org.aincraft.container.SmaugRecipe.RecipeResult.Status;
import org.aincraft.container.ingredient.Ingredient;
import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.container.item.IKeyedItem;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SmaugRecipe(IKeyedItem output, IngredientList ingredientList,
                          NamespacedKey recipeKey, NamespacedKey stationKey,
                          @Nullable String permission) implements Keyed {

  public RecipeResult test(Player player, Inventory inventory) {
    if (permission != null && !player.hasPermission(permission)) {
      return new RecipeResult(Status.FAILURE, null, "permission failure");
    }
    for (Ingredient ingredient : ingredientList) {
      if (!ingredient.isSubset(player, inventory)) {
        return new RecipeResult(Status.FAILURE, ingredientList.findMissing(player, inventory),
            null);
      }
    }
    return new RecipeResult(Status.SUCCESS, null, null);
  }

  @Override
  public @NotNull NamespacedKey getKey() {
    return recipeKey;
  }

  public IngredientList getIngredientList() {
    return ingredientList;
  }

  public IKeyedItem getOutput() {
    return output;
  }

  public record RecipeResult(@NotNull SmaugRecipe.RecipeResult.Status status,
                             @Nullable IngredientList missing,
                             @Nullable String errorMessage) {

    public enum Status {
      SUCCESS,
      FAILURE
    }
  }
}
