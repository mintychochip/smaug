/*
 *
 * Copyright (C) 2025 mintychochip
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.aincraft.database.model.meta;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import org.aincraft.database.model.Station.StationInventory;
import org.aincraft.database.model.meta.TrackableProgressMeta.Builder;
import org.jetbrains.annotations.Nullable;

public final class TrackableProgressMeta implements
    BuildableMeta<TrackableProgressMeta, Builder> {

  private final AtomicReference<String> recipeKeyReference;
  private final AtomicReference<Float> progressReference;
  private final AtomicReference<StationInventory> inventoryReference;

  public TrackableProgressMeta(String recipeKey, float progress,
      StationInventory inventory) {
    recipeKeyReference = new AtomicReference<>(recipeKey);
    progressReference = new AtomicReference<>(progress);
    inventoryReference = new AtomicReference<>(inventory);
  }

  @Override
  public TrackableProgressMeta clone() {
    return new TrackableProgressMeta(recipeKeyReference.get(), progressReference.get(),
        inventoryReference.get());
  }

  public void setProgress(float progress) {
    progressReference.set(progress);
  }

  public void setProgress(Function<Float, Float> progressConsumer) {
    float progress = progressConsumer.apply(this.getProgress());
    this.setProgress(progress);
  }

  public void setRecipeKey(@Nullable String recipeKey) {
    recipeKeyReference.set(recipeKey);
  }

  public void setInventory(StationInventory inventory) {
    inventoryReference.set(inventory);
  }

  @Nullable
  public String getRecipeKey() {
    return recipeKeyReference.get();
  }

  @Nullable
  public StationInventory getInventory() {
    return inventoryReference.get();
  }

  public float getProgress() {
    return progressReference.get();
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this.getRecipeKey(), this.getProgress(),
        this.getInventory());
  }

  public static final class Builder implements
      BuildableMeta.Builder<TrackableProgressMeta, Builder> {

    private String recipeKey;
    private float progress;
    private StationInventory inventory;

    Builder(String recipeKey, float progress,
        StationInventory inventory) {
      this.recipeKey = recipeKey;
      this.progress = progress;
      this.inventory = inventory;
    }

    public Builder setRecipeKey(String recipeKey) {
      this.recipeKey = recipeKey;
      return this;
    }

    public Builder setProgress(float progress) {
      this.progress = progress;
      return this;
    }

    public Builder setInventory(
        StationInventory inventory) {
      this.inventory = inventory;
      return this;
    }

    @Override
    public TrackableProgressMeta build() {
      return new TrackableProgressMeta(recipeKey, progress, inventory);
    }
  }
}
