package org.aincraft.container;

import org.aincraft.container.ingredient.IngredientList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RecipeResult {

  public enum Status {
    SUCCESS,
    FAILURE;
  }
  private final Status status;
  private final IngredientList missing;
  private final String error;

  RecipeResult(@NotNull Status status, @Nullable IngredientList missing,
      @Nullable String error) {
    this.status = status;
    this.missing = missing;
    this.error = error;
  }

  public Status getStatus() {
    return status;
  }

  public IngredientList getMissing() {
    return missing;
  }

  public String getError() {
    return error;
  }
}
