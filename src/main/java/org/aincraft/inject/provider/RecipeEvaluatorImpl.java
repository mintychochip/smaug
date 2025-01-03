package org.aincraft.inject.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.aincraft.container.IRecipeEvaluator;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.ingredient.Ingredient;
import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.listener.StationService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
class RecipeEvaluatorImpl implements IRecipeEvaluator {

  private final StationService service;

  @Inject
  public RecipeEvaluatorImpl(StationService service) {
    this.service = service;
  }


  @Override
  public RecipeResult test(SmaugRecipe recipe, Player player, Inventory inventory) {
    if (recipe.getPermission() != null && !player.hasPermission(recipe.getPermission())) {
      return new RecipeResultImpl(Status.PERMISSION_FAILURE, null, "permission failure");
    }
    for (Ingredient ingredient : recipe.getIngredientList()) {
      if (!ingredient.test(player, inventory)) {
        return new RecipeResultImpl(Status.INSUFFICIENT_RESOURCES,
            recipe.getIngredientList().findMissing(player, inventory),
            null);
      }
    }
    return new RecipeResultImpl(Status.SUCCESS, null, null);
  }

  private record RecipeResultImpl(@NotNull Status status, @Nullable IngredientList missing,
                          @Nullable String error) implements RecipeResult {

    @Override
    public @NotNull Status getStatus() {
      return status;
    }

    @Override
    public @Nullable IngredientList getMissing() {
      return missing;
    }

    @Override
    public @Nullable String getError() {
      return error;
    }
  }

}
