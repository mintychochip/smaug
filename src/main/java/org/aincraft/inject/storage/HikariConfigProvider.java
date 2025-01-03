package org.aincraft.inject.storage;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariConfig;
import org.aincraft.config.PluginConfiguration;
import org.aincraft.database.storage.StorageType;
import org.bukkit.configuration.ConfigurationSection;

final class HikariConfigProvider implements Provider<HikariConfig> {

  private final PluginConfiguration pluginConfiguration;

  @Inject
  public HikariConfigProvider(
      @Named("main") PluginConfiguration pluginConfiguration) {
    this.pluginConfiguration = pluginConfiguration;
  }

  @Override
  public HikariConfig get() {
    HikariConfig config = new HikariConfig();
    if(!pluginConfiguration.contains("storage")) {
      return config;
    }
    ConfigurationSection storageSection = pluginConfiguration.getConfigurationSection(
        "storage");
    if(storageSection == null) {
      return config;
    }
    StorageType type = StorageType.fromIdentifier(storageSection.getString("type", null));
    config.setUsername(storageSection.getString("username", null));
    config.setPassword(storageSection.getString("password", null));
    config.setDriverClassName(type.getClassName());
    config.setMaximumPoolSize(storageSection.getInt("pool-size", 10));
    config.setMinimumIdle(storageSection.getInt("min-idle", 10));
    config.setConnectionTimeout(storageSection.getInt("connection-timeout", 30000));
    config.setIdleTimeout(storageSection.getInt("idle-timeout", 600000));
    config.setMaxLifetime(storageSection.getInt("max-lifetime", 1800000));
    config.setLeakDetectionThreshold(
        storageSection.getInt("leak-detection-threshold", 0));
    return config;
  }
}
