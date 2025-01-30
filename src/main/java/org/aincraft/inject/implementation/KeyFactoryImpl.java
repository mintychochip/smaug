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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.aincraft.inject.IKeyFactory;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

@Singleton
final class KeyFactoryImpl implements IKeyFactory {

  private final Plugin plugin;

  @Inject
  public KeyFactoryImpl(Plugin plugin) {
    this.plugin = plugin;
  }

  @Override
  @Nullable
  public NamespacedKey resolveKey(String keyString, boolean minecraft) {
    if (keyString == null) {
      return null;
    }
    String[] splitKey = keyString.toLowerCase().split(":");
    if (splitKey.length == 1) {
      if (minecraft && this.isMinecraftResource(keyString)) {
        return NamespacedKey.minecraft(keyString);
      }
      return new NamespacedKey(plugin, keyString);
    }
    return NamespacedKey.fromString(keyString);
  }

  private boolean isMinecraftResource(String resource) {
    Material material = Material.getMaterial(resource.toUpperCase());
    return material != null;
  }
}
