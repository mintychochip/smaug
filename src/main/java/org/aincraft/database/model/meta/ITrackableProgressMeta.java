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

import com.google.common.base.Preconditions;
import java.util.function.Function;
import org.aincraft.database.model.meta.ITrackableProgressMeta.Builder;
import org.aincraft.database.model.meta.TrackableProgressMetaImpl.StationInventory;
import org.aincraft.database.model.test.BuildableMeta;
import org.aincraft.database.storage.IConnectionSource;
import org.aincraft.database.storage.SqlExecutor;
import org.jetbrains.annotations.NotNull;

public interface ITrackableProgressMeta extends BuildableMeta<ITrackableProgressMeta, Builder>,
    StationInventoryHolder {

  static MetaMapping<ITrackableProgressMeta> createMapping(IConnectionSource source) {
    return new TrackableProgressMetaImpl.TrackableProgressMetaMapping(new SqlExecutor(source));
  }

  String getRecipeKey();

  float getProgress();

  StationInventory getInventory();

  void setRecipeKey(String recipeKey);

  void setProgress(float progress);

  default void setProgress(@NotNull Function<Float,Float> progressFunction) {
    Preconditions.checkNotNull(progressFunction);
    float progress = this.getProgress();
    this.setProgress(progressFunction.apply(progress));
  }

  void setInventory(StationInventory inventory);

  interface Builder extends BuildableMeta.Builder<ITrackableProgressMeta, Builder> {

    Builder setRecipeKey(String recipeKey);

    Builder setProgress(float progress);

    Builder setInventory(StationInventory inventory);
  }
}
