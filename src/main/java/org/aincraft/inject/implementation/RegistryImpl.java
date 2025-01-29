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

