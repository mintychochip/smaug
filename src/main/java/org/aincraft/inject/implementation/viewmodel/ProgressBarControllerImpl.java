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
import net.kyori.adventure.bossbar.BossBar;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.display.IViewModel;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationMeta;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@Singleton
final class ProgressBarControllerImpl extends AbstractViewModelController<Station, BossBar> {

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
    IViewModel<Station, BossBar> viewModel = this.get(
        model.stationKey());
    if (viewModel == null) {
      return;
    }
    viewModel.update(model);
  }
}
