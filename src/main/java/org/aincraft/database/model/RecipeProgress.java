package org.aincraft.database.model;

import java.util.UUID;

public final class RecipeProgress {

  private final String idString;
  private final String stationId;
  private String recipeKey;
  private float progress;

  public RecipeProgress(String idString, String stationId, String recipeKey, float progress) {
    this.idString = idString;
    this.stationId = stationId;
    this.recipeKey = recipeKey;
    this.progress = progress;
  }

  public void setRecipeKey(String recipeKey) {
    this.recipeKey = recipeKey;
  }

  public void setProgress(float progress) {
    this.progress = progress;
  }

  public float getProgress() {
    return progress;
  }

  public UUID getStationId() {
    return UUID.fromString(stationId);
  }

  public UUID getId() {
    return UUID.fromString(idString);
  }

  public String getRecipeKey() {
    return recipeKey;
  }

  public void increment(int i) {
    this.progress += i;
  }
}
