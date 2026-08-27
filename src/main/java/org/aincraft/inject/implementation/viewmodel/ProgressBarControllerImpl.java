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
import org.aincraft.api.event.StationRemoveEvent;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.display.ViewModel;
import org.aincraft.container.display.ViewModelController;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationMeta;
import org.aincraft.inject.implementation.viewmodel.ProgressBarViewModel.BossBarBinding;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

/**
 * Event fan for progress boss bars: state sync only if already shown (bound).
 */
@Singleton
public final class ProgressBarControllerImpl extends ViewModelController<Station, BossBarBinding> {

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleUpdate(final StationUpdateEvent event) {
    if (event.isCancelled()) {
      return;
    }
    final Station model = event.getModel();
    StationMeta meta = model.getMeta();
    String recipeKey = meta.getRecipeKey();
    if (recipeKey == null) {
      return;
    }
    ViewModel<Station, BossBarBinding> viewModel = this.get(model.stationKey());
    if (viewModel == null) {
      return;
    }
    viewModel.updateIfBound(model);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleRemove(final StationRemoveEvent event) {
    if (event.isCancelled()) {
      return;
    }
    Station station = event.getStation();
    ViewModel<Station, BossBarBinding> viewModel = this.get(station.stationKey());
    if (viewModel instanceof ProgressBarViewModel progressBarViewModel) {
      progressBarViewModel.removeStation(station);
    } else if (viewModel != null) {
      viewModel.remove(station);
    }
  }
}
