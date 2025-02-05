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

package org.aincraft.inject.implementation.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.key.Key;
import org.aincraft.api.event.StationRemoveEvent;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.display.IViewModel;
import org.aincraft.database.model.meta.Meta;
import org.aincraft.database.model.test.IMetaStation;
import org.aincraft.listener.IMetaStationDatabaseService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public final class MetaListener<M extends Meta<M>, E extends StationUpdateEvent<M>> implements
    IUpdateListener<M, E> {

  private final Class<M> metaClass;
  private final IMetaStationDatabaseService<M> databaseService;

  public MetaListener(Class<M> metaClass, IMetaStationDatabaseService<M> databaseService) {
    this.metaClass = metaClass;
    this.databaseService = databaseService;
  }

  private final Map<Key, List<IViewModel<IMetaStation<M>>>> viewModels = new HashMap<>();

  @EventHandler(priority = EventPriority.MONITOR)
  public void onUpdateMetaStation(E event) {
    if (event.isCancelled()) {
      return;
    }

    IMetaStation<M> station = event.getStation();
    List<IViewModel<IMetaStation<M>>> vms = this.viewModels.getOrDefault(station.getKey(),
        new ArrayList<>());
    if (vms.isEmpty()) {
      return;
    }
    databaseService.updateStation(station);
    vms.forEach(v -> v.update(station));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onRemoveMetaStation(StationRemoveEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (!(event.getStation() instanceof IMetaStation<?> metaStation && this.getMetaClass()
        .isAssignableFrom(metaStation.getMeta().getClass()))) {
      return;
    }
    List<IViewModel<IMetaStation<M>>> vms = this.viewModels.getOrDefault(
        metaStation.getKey(),
        new ArrayList<>());
    if (vms.isEmpty()) {
      return;
    }
    @SuppressWarnings("unchecked")
    IMetaStation<M> station = (IMetaStation<M>) event.getStation();
    vms.forEach(v -> v.remove(station));
  }

  public void register(Key key, List<IViewModel<IMetaStation<M>>> viewModels) {
    this.viewModels.put(key, viewModels);
  }

  @Override
  public Class<M> getMetaClass() {
    return metaClass;
  }
}
