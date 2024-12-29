package org.aincraft.config;

import java.util.List;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;

public interface PluginConfiguration {
  void reload();
  void save();
  Set<String> getKeys(boolean deep);
  ConfigurationSection getConfigurationSection(String path);
  String getString(String path);
  String getString(String path, String def);
  int getInt(String path);
  int getInt(String path, int def);
  boolean getBoolean(String path);
  List<String> getStringList(String path);
}
