package org.aincraft.database.model;

import java.util.UUID;
import org.bukkit.NamespacedKey;

public final class StationRecipeProgress {

  private final String id;
  private final String stationId;
  private final String recipeKey;
  private int progress;

  public StationRecipeProgress(String id, String stationId, String recipeKey, int progress) {
    this.id = id;
    this.stationId = stationId;
    this.recipeKey = recipeKey;
    this.progress = progress;
  }

  public void setProgress(int progress) {
    this.progress = progress;
  }

  public int getProgress() {
    return progress;
  }

  public UUID getStationId() {
    return UUID.fromString(stationId);
  }

  public UUID getId() {
    return UUID.fromString(id);
  }

  public NamespacedKey getRecipeKey() {
    return NamespacedKey.fromString(recipeKey);
  }
}
