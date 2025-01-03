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
