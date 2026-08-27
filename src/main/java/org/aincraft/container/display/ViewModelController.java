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

package org.aincraft.container.display;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.kyori.adventure.key.Key;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Station-type key → projection registry. Subclasses listen for domain events and
 * fan out to the registered {@link ViewModel}. Not an MVC/MVVM "controller" in the
 * UI-framework sense — interaction logic lives in {@code StationHandler}.
 */
public class ViewModelController<M, B> implements Iterable<ViewModel<M, B>>, Listener {

  protected final Map<Key, ViewModel<M, B>> viewModels = new HashMap<>();

  public void register(@NotNull Key stationKey, @NotNull ViewModel<M, B> viewModel) {
    viewModels.put(stationKey, viewModel);
  }

  public boolean isRegistered(@NotNull Key stationKey) {
    return viewModels.containsKey(stationKey);
  }

  public @Nullable ViewModel<M, B> get(@NotNull Key stationKey) {
    return viewModels.get(stationKey);
  }

  public Collection<ViewModel<M, B>> getAll() {
    return viewModels.values();
  }

  /** Drop every binding on every registered projection (plugin disable). */
  public void clearAll() {
    for (ViewModel<M, B> viewModel : viewModels.values()) {
      viewModel.removeAll();
    }
  }

  @NotNull
  @Override
  public Iterator<ViewModel<M, B>> iterator() {
    return viewModels.values().iterator();
  }
}
