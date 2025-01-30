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

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.kyori.adventure.key.Key;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.container.gui.AnvilGuiProxy;
import org.aincraft.inject.implementation.view.AnvilGuiProxyParameterizedFactory;
import org.aincraft.listener.IStationService;
import org.bukkit.plugin.Plugin;

public class AnvilGuiControllerProvider implements
    Provider<IViewModelController<StationPlayerModelProxy, AnvilGuiProxy>> {

  private final IStationService stationService;
  private final Plugin plugin;

  @Inject
  public AnvilGuiControllerProvider(IStationService stationService, Plugin plugin) {
    this.stationService = stationService;
    this.plugin = plugin;
  }

  @Override
  public IViewModelController<StationPlayerModelProxy, AnvilGuiProxy> get() {
    AnvilGuiController controller = new AnvilGuiController(stationService, plugin);
    controller.register(Key.key("smaug:anvil"),new AnvilGuiViewModel(new AnvilGuiProxyParameterizedFactory(stationService,plugin)));
    return controller;
  }
}
