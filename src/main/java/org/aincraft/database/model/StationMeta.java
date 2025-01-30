///*
// *
// * Copyright (C) 2025 mintychochip
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// *
// */
//
//package org.aincraft.database.model;
//
//import java.util.concurrent.atomic.AtomicReference;
//import java.util.function.Function;
//import org.aincraft.database.model.Station.StationInventory;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//public class StationMeta {
//
//  private final AtomicReference<String> recipeKeyReference;
//  private final AtomicReference<Float> progressReference;
//  private final AtomicReference<StationInventory> inventoryReference;
//
//  public static StationMeta create(String recipeKey, float progress) {
//    return new StationMeta(recipeKey, progress, StationInventory.create());
//  }
//
//  public StationMeta(String recipeKey, float progress, StationInventory inventory) {
//    recipeKeyReference = new AtomicReference<>(recipeKey);
//    progressReference = new AtomicReference<>(progress);
//    inventoryReference = new AtomicReference<>(inventory);
//  }
//
//  public void setProgress(float progress) {
//    progressReference.set(progress);
//  }
//
//  public void setRecipeKey(String recipeKey) {
//    recipeKeyReference.set(recipeKey);
//  }
//
//  public void setInventory(StationInventory inventory) {
//    inventoryReference.set(inventory);
//  }
//
//  public float getProgress() {
//    return progressReference.get();
//  }
//
//  @Nullable
//  public String getRecipeKey() {
//    return recipeKeyReference.get();
//  }
//
//  @NotNull
//  public StationInventory getInventory() {
//    return inventoryReference.get();
//  }
//
//  public static final class Builder {
//
//    private String recipeKey;
//    private float progress;
//    private StationInventory stationInventory;
//
//    Builder(String recipeKey, float progress, StationInventory stationInventory) {
//      this.recipeKey = recipeKey;
//      this.progress = progress;
//      this.stationInventory = stationInventory;
//    }
//
//    public Builder setRecipeKey(String recipeKey) {
//      this.recipeKey = recipeKey;
//      return this;
//    }
//
//    public Builder setProgress(float progress) {
//      this.progress = progress;
//      return this;
//    }
//
//    public Builder setProgress(Function<Float, Float> progressFunction) {
//      this.progress = progressFunction.apply(progress);
//      return this;
//    }
//
//    public Builder setInventory(
//        StationInventory stationInventory) {
//      this.stationInventory = stationInventory;
//      return this;
//    }
//
//    public StationMeta build() {
//      return new StationMeta(recipeKey, progress, stationInventory);
//    }
//  }
//}
