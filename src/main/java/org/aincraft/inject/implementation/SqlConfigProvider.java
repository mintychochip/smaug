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
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.aincraft.config.PluginConfiguration;
import org.aincraft.database.storage.SqlConfig;
import org.aincraft.database.storage.StorageType;
import org.bukkit.configuration.ConfigurationSection;

@Singleton
final class SqlConfigProvider implements Provider<SqlConfig> {

  private static final int MAX_POOL_SIZE = 10;
  private static final int MIN_IDLE = 10;
  private static final int MAX_LIFETIME = 1800000;
  private static final int CONNECTION_TIMEOUT = 30000;
  private static final int IDLE_TIMEOUT = 600000;
  private static final int LEAK_DETECTION_THRESHOLD = 0;

  private final PluginConfiguration pluginConfiguration;

  @Inject
  public SqlConfigProvider(PluginConfiguration pluginConfiguration) {
    this.pluginConfiguration = pluginConfiguration;
  }

  @Override
  public SqlConfig get() {
    if (!pluginConfiguration.contains("storage")) {
      return new SqlConfig(null, null, null, null, null, 0, 0, 0, null, 0, 0, 0);
    }
    ConfigurationSection storageSection = pluginConfiguration.getConfigurationSection("storage");
    return new SqlConfig.Builder()
        .setUsername(storageSection.getString("username"))
        .setPassword(storageSection.getString("password"))
        .setDatabaseName(storageSection.getString("db-name"))
        .setHostname(storageSection.getString("hostname"))
        .setMaxLifetime(storageSection.getInt("max-lifetime", MAX_LIFETIME))
        .setStorageType(
            StorageType.fromIdentifier(storageSection.getString("type")))
        .setPoolSize(storageSection.getInt("pool-size", MAX_POOL_SIZE))
        .setMinIdle(storageSection.getInt("min-idle", MIN_IDLE))
        .setConnectionTimeout(
            storageSection.getInt("connection-timeout", CONNECTION_TIMEOUT))
        .setIdleTimeout(storageSection.getInt("idle-timeout", IDLE_TIMEOUT))
        .setLeakDetectionThreshold(storageSection.getInt("leak-detection-threshold",
            LEAK_DETECTION_THRESHOLD)).build();
  }
}
