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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class YamlConfig implements PluginConfiguration {

  private final String resourcePath;
  private final Plugin plugin;
  private YamlConfiguration config = new YamlConfiguration();
  private File file;

  public YamlConfig(String resourcePath, Plugin plugin) {
    this.resourcePath = resourcePath;
    this.plugin = plugin;
    this.file = new File(plugin.getDataFolder(), resourcePath);
    if (!file.exists()) {
      plugin.saveResource(resourcePath, false);
    }

    config.options().parseComments(true);
    config = YamlConfiguration.loadConfiguration(file);
  }

  @Override
  public void reload() {
    try {
      file = new File(plugin.getDataFolder(), resourcePath);
      config = YamlConfiguration.loadConfiguration(file);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void save() {
    try {
      config.save(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean contains(String path) {
    return config.contains(path);
  }

  @Override
  public Set<String> getKeys(boolean deep) {
    return config.getKeys(deep);
  }

  @Override
  public ConfigurationSection getConfigurationSection(String path) {
    return config.getConfigurationSection(path);
  }

  @Override
  public String getString(String path) {
    return config.getString(path);
  }

  @Override
  public String getString(String path, String def) {
    return config.getString(path, def);
  }

  @Override
  public int getInt(String path) {
    return config.getInt(path);
  }

  @Override
  public int getInt(String path, int def) {
    return config.getInt(path, def);
  }

  @Override
  public boolean getBoolean(String path) {
    return config.getBoolean(path);
  }

  @Override
  public List<String> getStringList(String path) {
    return config.getStringList(path);
  }
}
