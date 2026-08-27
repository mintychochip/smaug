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
import org.aincraft.container.display.ViewModelController;
import org.aincraft.database.model.Station;
import org.aincraft.inject.IRecipeFetcher;
import org.aincraft.inject.implementation.viewmodel.ProgressBarViewModel.BossBarBinding;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class ProgressBarControllerProvider implements
    Provider<ViewModelController<Station, BossBarBinding>> {

  private final IRecipeFetcher recipeFetcher;
  private final Plugin plugin;

  @Inject
  public ProgressBarControllerProvider(IRecipeFetcher recipeFetcher, Plugin plugin) {
    this.recipeFetcher = recipeFetcher;
    this.plugin = plugin;
  }

  @Override
  public ViewModelController<Station, BossBarBinding> get() {
    ProgressBarControllerImpl controller = new ProgressBarControllerImpl();
    controller.register(new NamespacedKey(plugin, "anvil"), new ProgressBarViewModel(recipeFetcher));
    return controller;
  }
}
