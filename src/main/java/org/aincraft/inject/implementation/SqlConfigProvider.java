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
