package org.aincraft.exception;

/**
 * <p>
 * Thrown when a key for a recipe is not present in the configuration.
 * </p>
 */
public class UndefinedRecipeException extends Exception {

  private final String recipeKey;

  public UndefinedRecipeException(String recipeKey) {
    super("the recipe: %s is undefined in the configuration".formatted(recipeKey));
    this.recipeKey = recipeKey;
  }

  public String getRecipeKey() {
    return recipeKey;
  }
}
