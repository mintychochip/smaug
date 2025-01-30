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

import com.google.inject.Singleton;
import java.util.List;
import java.util.function.Consumer;
import org.aincraft.api.event.StationRemoveEvent;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.display.AnvilItemDisplayView;
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.display.IViewModel.IViewModelBinding;
import org.aincraft.container.display.PropertyNotFoundException;
import org.aincraft.database.model.Station;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@Singleton
final class ItemDisplayControllerImpl extends
    AbstractViewModelController<Station, AnvilItemDisplayView> {

  private static final Consumer<IViewModelBinding> REMOVE_ENTITY_CONSUMER = binding -> {
    try {
      @SuppressWarnings("unchecked")
      List<Display> displays = (List<Display>) binding.getProperty(
          "displays", List.class);
      displays.forEach(Entity::remove);
    } catch (PropertyNotFoundException e) {
      throw new RuntimeException(e);
    }
  };


  @EventHandler(priority = EventPriority.MONITOR)
  private void handleUpdateItemDisplay(final StationUpdateEvent event) {
    if (event.isCancelled()) {
      return;
    }
    final Station model = event.getModel();
    final IViewModel<Station, AnvilItemDisplayView> viewModel = this.get(model.stationKey());
    if(viewModel == null) {
      return;
    }
    viewModel.update(model);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleRemoveItemDisplay(final StationRemoveEvent event) {
    if (event.isCancelled()) {
      return;
    }
    Station station = event.getStation();
    IViewModel<Station, AnvilItemDisplayView> viewModel = this.get(station.stationKey());
    if (viewModel == null) {
      return;
    }
    viewModel.remove(station, REMOVE_ENTITY_CONSUMER);
  }
}
