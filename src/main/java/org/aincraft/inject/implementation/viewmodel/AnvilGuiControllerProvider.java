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

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.kyori.adventure.key.Key;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.container.gui.AnvilGuiProxy;
import org.aincraft.inject.implementation.view.AnvilGuiProxyFactory;
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
    controller.register(Key.key("smaug:anvil"),new AnvilGuiViewModel(new AnvilGuiProxyFactory(stationService,plugin)));
    return controller;
  }
}
