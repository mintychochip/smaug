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

package org.aincraft.config;

import org.bukkit.plugin.Plugin;

public final class ConfigurationFactory {

  private final Plugin plugin;

  public ConfigurationFactory(Plugin plugin) {
    this.plugin = plugin;
  }

  public PluginConfiguration create(String filePath) {
    String[] split = filePath.split("\\.");
    if (split.length < 2) {
      return null;
    }
    if (split[1].equals("yml")) {
      return new YamlConfig(filePath, plugin);
    }
    return null;
  }
}
