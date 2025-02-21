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
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.display.IViewModel;
import org.aincraft.database.model.meta.IMeta;
import org.aincraft.database.model.test.IMetaStation;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public final class StationPlayerProxyViewController<M extends IMeta<M>, E extends StationUpdateEvent<M>> extends
    AbstractViewModelController<StationPlayerProxy<M>> {

  @EventHandler(priority = EventPriority.NORMAL)
  private void onUpdate(E event) {
    if (event.isCancelled()) {
      return;
    }
    IMetaStation<M> station = event.getStation();
    List<IViewModel<StationPlayerProxy<M>>> vms = viewModels.getOrDefault(station.getKey(),
        new ArrayList<>());
    if (vms.isEmpty()) {
      return;
    }
    vms.forEach(vm -> vm.update(new StationPlayerProxy<>(event.getViewer(), station)));
  }
}
