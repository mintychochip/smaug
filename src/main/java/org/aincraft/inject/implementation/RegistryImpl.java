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

package org.aincraft.inject.implementation;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.aincraft.container.IRegistry;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.IKeyedItemFactory;
import org.aincraft.inject.IKeyFactory;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class RegistryImpl<T extends Keyed> implements IRegistry<T> {

  protected Map<NamespacedKey, T> registry = new ConcurrentHashMap<>();

  @Override
  public void register(T object) {
    registry.put(object.getKey(), object);
  }

  @Override
  public Stream<T> stream() {
    return registry.values().stream();
  }

  @Override
  public Iterator<T> iterator() {
    return registry.values().iterator();
  }

  @Override
  public Optional<T> get(NamespacedKey key) {
    return Optional.ofNullable(registry.get(key));
  }

  @Singleton
  static final class ItemRegistryImpl extends RegistryImpl<IKeyedItem> implements
      IItemRegistry {

    private final IKeyFactory keyFactory;
    private final IKeyedItemFactory keyedItemFactory;
    ItemRegistryImpl(IKeyFactory keyFactory, IKeyedItemFactory keyedItemFactory) {
      this.keyFactory = keyFactory;
      this.keyedItemFactory = keyedItemFactory;
    }

    @Override
    public boolean check(@NotNull String key, boolean minecraft) {
      Preconditions.checkNotNull(key);
      return this.resolve(key,minecraft) != null;
    }

    @Override
    public @Nullable IKeyedItem resolve(String key, boolean minecraft) {
      NamespacedKey namespacedKey = keyFactory.resolveKey(key, minecraft);
      return resolve(namespacedKey, minecraft);
    }

    @Override
    public IKeyedItem resolve(NamespacedKey key, boolean minecraft) {
      if (key == null) {
        return null;
      }
      Optional<IKeyedItem> keyedItem = this.get(key);
      if (keyedItem.isPresent()) {
        return keyedItem.get();
      }
      if (minecraft && key.getNamespace().equals("minecraft")) {
        Material material = Registry.MATERIAL.get(key);
        if (material != null) {
          return keyedItemFactory.create(new ItemStack(material),key);
        }
      }
      return null;
    }
  }
}

