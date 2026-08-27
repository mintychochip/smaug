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

import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.display.ViewModel;
import org.aincraft.container.display.ViewModelController;
import org.aincraft.database.model.Station;
import org.aincraft.inject.implementation.viewmodel.AnvilGuiViewModel.AnvilGuiBinding;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

/**
 * Event fan for anvil GUI: refreshes only if the player×station GUI was already opened.
 */
public class AnvilGuiController extends ViewModelController<StationPlayerModelProxy, AnvilGuiBinding> {

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleUpdate(final StationUpdateEvent event) {
    if (event.isCancelled()) {
      return;
    }
    Station model = event.getModel();
    Player player = event.getViewer();
    if (player == null) {
      return;
    }
    ViewModel<StationPlayerModelProxy, AnvilGuiBinding> viewModel = this.get(model.stationKey());
    if (viewModel == null) {
      return;
    }
    // Do not create GUI resources on every station update — only refresh open sessions
    viewModel.updateIfBound(new StationPlayerModelProxy(player, model));
  }
}
