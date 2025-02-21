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
import java.util.List;
import org.aincraft.api.event.StationRemoveEvent;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.display.IViewModel;
import org.aincraft.database.model.meta.IMeta;
import org.aincraft.database.model.test.IMetaStation;
import org.bukkit.event.EventHandler;

public final class StationModelViewController<M extends IMeta<M>, E extends StationUpdateEvent<M>> extends
    AbstractViewModelController<IMetaStation<M>> {

  private final Class<M> metaClazz;

  public StationModelViewController(Class<M> metaClazz) {
    this.metaClazz = metaClazz;
  }

  public Class<M> getMetaClazz() {
    return metaClazz;
  }

  @EventHandler
  private void onUpdate(E event) {
    if (event.isCancelled()) {
      return;
    }

    IMetaStation<M> station = event.getStation();
    List<IViewModel<IMetaStation<M>>> vms = viewModels.getOrDefault(station.getKey(), new ArrayList<>());
    if(vms.isEmpty()) {
      return;
    }
    vms.forEach(vm -> vm.update(station));
  }

  @EventHandler
  private void onRemove(StationRemoveEvent event) {
    if(event.isCancelled()) {
      return;
    }

    if (!(event.getStation() instanceof IMetaStation<?> metaStation && getMetaClazz()
        .isAssignableFrom(metaStation.getMeta().getClass()))) {
      return;
    }

    List<IViewModel<IMetaStation<M>>> vms = viewModels.getOrDefault(metaStation.getKey(),
        new ArrayList<>());
    if(vms.isEmpty()) {
      return;
    }
    IMetaStation<M> station = (IMetaStation<M>) event.getStation();
    vms.forEach(vm -> vm.remove(station));
  }

}
