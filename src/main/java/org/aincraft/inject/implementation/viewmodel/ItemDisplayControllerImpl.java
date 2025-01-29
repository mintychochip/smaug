/*
 * MIT License
 *
 * Copyright (c) 2025 mintychochip
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * provided to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
