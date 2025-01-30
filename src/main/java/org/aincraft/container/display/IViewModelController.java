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
import net.kyori.adventure.key.Key;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public interface IViewModelController<M, V> extends Iterable<IViewModel<M, V>>, Listener {

  void register(@NotNull Key stationKey, @NotNull IViewModel<M, V> viewModel);

  boolean isRegistered(@NotNull Key stationKey);

  IViewModel<M, V> get(@NotNull Key stationKey);

  Collection<IViewModel<M, V>> getAll();
}
