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

package org.aincraft.container;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;
import org.aincraft.container.item.IKeyedItem;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRegistry<T> {

  void register(T object);

  Optional<T> get(NamespacedKey key);

  Stream<T> stream();

  Iterator<T> iterator();

  interface IItemRegistry extends IRegistry<IKeyedItem> {

    boolean check(@NotNull String key, boolean minecraft);
    @Nullable
    IKeyedItem resolve(@Nullable String key, boolean minecraft);

    @Nullable
    default IKeyedItem resolve(@Nullable String key) {
      return resolve(key, false);
    }

    @Nullable
    IKeyedItem resolve(@Nullable NamespacedKey key, boolean minecraft);

    @Nullable
    default IKeyedItem resolve(@Nullable NamespacedKey key) {
      return resolve(key, false);
    }
  }
}
