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
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.gui.AnvilGuiProxy;
import org.aincraft.database.model.Station;
import org.aincraft.inject.implementation.view.AnvilGuiProxyFactory;
import org.aincraft.listener.IStationService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

public class AnvilGuiController extends
    AbstractViewModelController<StationPlayerModelProxy, AnvilGuiProxy> {


  private final Plugin plugin;
  private final IStationService stationService;

  public AnvilGuiController(IStationService stationService, Plugin plugin) {
    this.stationService = stationService;
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleUpdate(final StationUpdateEvent event) {
    if (event.isCancelled()) {
      return;
    }
    Station model = event.getModel();
    Player player = event.getViewer();
    StationPlayerModelProxy proxy = new StationPlayerModelProxy(player, model);
    IViewModel<StationPlayerModelProxy, AnvilGuiProxy> viewModel = this.get(model.stationKey());
    if (!viewModel.isBound(proxy)) {
      viewModel.bind(proxy, new AnvilGuiProxyFactory(stationService, plugin).create(proxy));
    }
    viewModel.update(new StationPlayerModelProxy(player, model));
  }
}
