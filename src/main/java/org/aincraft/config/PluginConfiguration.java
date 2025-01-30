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

import java.util.List;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;

public interface PluginConfiguration {
  void reload();
  void save();
  boolean contains(String path);
  Set<String> getKeys(boolean deep);
  ConfigurationSection getConfigurationSection(String path);
  String getString(String path);
  String getString(String path, String def);
  int getInt(String path);
  int getInt(String path, int def);
  boolean getBoolean(String path);
  List<String> getStringList(String path);
}
