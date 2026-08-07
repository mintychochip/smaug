package org.aincraft.container.refining;

import java.util.List;
import java.util.Objects;
import org.aincraft.container.ingredient.Ingredient;
import org.aincraft.container.ingredient.IngredientList;
import org.jetbrains.annotations.Nullable;

public final class RefiningPreview {

  private final int stationTier;
  private final int professionLevel;
  private final @Nullable String selectedRecipeKey;
  private final int requestedBatch;
  private final IngredientList primaryIngredients;
  private final IngredientList reagents;
  private final int baseOutputAmount;
  private final int possibleOutputAmount;
  private final RefiningResult.Status status;

  public RefiningPreview(int stationTier, int professionLevel,
      @Nullable String selectedRecipeKey, int requestedBatch,
      IngredientList primaryIngredients, IngredientList reagents,
      int baseOutputAmount, int possibleOutputAmount, RefiningResult.Status status) {
    if (stationTier < 0 || professionLevel < 0 || requestedBatch < 0
        || baseOutputAmount < 0 || possibleOutputAmount < 0
        || possibleOutputAmount < baseOutputAmount) {
      throw new IllegalArgumentException("invalid refining preview values");
    }
    this.stationTier = stationTier;
    this.professionLevel = professionLevel;
    this.selectedRecipeKey = selectedRecipeKey;
    this.requestedBatch = requestedBatch;
    this.primaryIngredients = copy(Objects.requireNonNull(primaryIngredients, "primaryIngredients"));
    this.reagents = copy(Objects.requireNonNull(reagents, "reagents"));
    this.baseOutputAmount = baseOutputAmount;
    this.possibleOutputAmount = possibleOutputAmount;
    this.status = Objects.requireNonNull(status, "status");
  }

  public int stationTier() {
    return stationTier;
  }

  public int professionLevel() {
    return professionLevel;
  }

  public @Nullable String selectedRecipeKey() {
    return selectedRecipeKey;
  }

  public int requestedBatch() {
    return requestedBatch;
  }

  public IngredientList primaryIngredients() {
    return copy(primaryIngredients);
  }

  public IngredientList reagents() {
    return copy(reagents);
  }

  public int baseOutputAmount() {
    return baseOutputAmount;
  }

  public int possibleOutputAmount() {
    return possibleOutputAmount;
  }

  public RefiningResult.Status status() {
    return status;
  }

  public boolean canExecute() {
    return status == RefiningResult.Status.SUCCESS;
  }

  private static IngredientList copy(IngredientList source) {
    List<Ingredient> ingredients = source.asList();
    return new IngredientList(List.copyOf(ingredients));
  }
}
