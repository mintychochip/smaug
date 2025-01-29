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
