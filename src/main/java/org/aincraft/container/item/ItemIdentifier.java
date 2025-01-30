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