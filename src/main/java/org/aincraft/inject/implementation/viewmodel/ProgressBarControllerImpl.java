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
import net.kyori.adventure.bossbar.BossBar;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.display.IViewModel;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationMeta;
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
