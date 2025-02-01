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

package org.aincraft.inject.implementation;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.aincraft.database.model.meta.TrackableProgressMeta;
import org.aincraft.database.storage.CachedMutableStationService;
import org.aincraft.database.storage.IConnectionSource;
import org.aincraft.database.storage.SqlExecutor;
import org.aincraft.listener.IMutableStationService;

public class MetaServiceProvider implements Provider<IMutableStationService<TrackableProgressMeta>> {

  private final IConnectionSource source;
  @Inject
  public MetaServiceProvider(IConnectionSource source) {
    this.source = source;
  }
  @Override
  public IMutableStationService<TrackableProgressMeta> get() {
    SqlExecutor executor = new SqlExecutor(source);
    return new CachedMutableStationService<>(source,TrackableProgressMeta.createMapping(executor));
  }
}
