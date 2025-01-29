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

package org.aincraft.container.item;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ItemIdentifier implements Keyed {

  private final NamespacedKey key;
  private final long version;

  private final Map<Long, ItemIdentifier> aliasMap = new HashMap<>();

  public ItemIdentifier(NamespacedKey key, long version) {
    this.key = key;
    this.version = version;
  }

  //TODO: work on alias for migrations prio low
  @Experimental
  public ItemIdentifier addAlias(ItemIdentifier identifier) {
    aliasMap.put(identifier.getVersion(), identifier);
    return this;
  }

  public boolean isAlias(ItemIdentifier identifier) {
    return aliasMap.entrySet().stream().anyMatch(entry -> entry.getValue().equals(identifier));
  }

  @Override
  public @NotNull NamespacedKey getKey() {
    return key;
  }

  public long getVersion() {
    return version;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NamespacedKey namespacedKey) {
      return namespacedKey.equals(this.key);
    }
    if (obj instanceof Keyed keyed) {
      return keyed.getKey().equals(this.key);
    }
    return obj.equals(this);
  }

  public static boolean contains(ItemStack itemStack, NamespacedKey identifierKey, String substr) {
    ItemIdentifier identifier = getIdentifier(itemStack, identifierKey);
    if (identifier == null) {
      return false;
    }
    return identifier.getKey().getKey().contains(substr);
  }

  public static boolean compare(@NotNull ItemStack one, @NotNull ItemStack two,
      @NotNull NamespacedKey identifierKey) {
    ItemIdentifier oneIdentifier = getIdentifier(one, identifierKey);
    ItemIdentifier twoIdentifier = getIdentifier(two, identifierKey);
    if (oneIdentifier == null || twoIdentifier == null) {
      return false;
    }
    return oneIdentifier.equals(twoIdentifier);
  }

  public static @Nullable ItemIdentifier getIdentifier(@Nullable ItemStack itemStack,
      @NotNull NamespacedKey identifierKey) {
    if (itemStack == null) {
      return null;
    }
    if (itemStack.getType().isAir()) {
      return null;
    }
    ItemMeta itemMeta = itemStack.getItemMeta();
    assert itemMeta != null;
    PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
    if (!pdc.has(identifierKey, PersistentDataType.STRING)) {
      return null;
    }
    String identifierString = pdc.get(identifierKey, PersistentDataType.STRING);
    return new Gson().fromJson(identifierString, ItemIdentifier.class);
  }
}