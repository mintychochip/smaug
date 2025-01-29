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
import com.google.inject.name.Named;
import java.util.logging.Logger;
import org.aincraft.config.PluginConfiguration;
import org.aincraft.database.storage.IStorage;
import org.aincraft.database.storage.SqlConfig;
import org.aincraft.database.storage.SqlStorageImpl;
import org.aincraft.database.storage.StorageType;
import org.aincraft.database.storage.flatfile.H2Source;
import org.aincraft.database.storage.flatfile.SqliteSource;
import org.aincraft.database.storage.hikari.HikariSource;
import org.bukkit.plugin.Plugin;

final class StorageProvider implements Provider<IStorage> {

  private final Logger logger;
  private final Plugin plugin;
  private final PluginConfiguration pluginConfiguration;
  private final SqlConfig sqlConfig;
  private final ResourceExtractor  extractor;

  @Inject
  public StorageProvider(@Named("logger") Logger logger, Plugin plugin,
      PluginConfiguration pluginConfiguration,
      SqlConfig sqlConfig, ResourceExtractor extractor) {
    this.logger = logger;
    this.plugin = plugin;
    this.pluginConfiguration = pluginConfiguration;
    this.sqlConfig = sqlConfig;
    this.extractor = extractor;
  }

  @Override
  public IStorage get() {
    StorageType type = StorageType.fromIdentifier(
        pluginConfiguration.getString("storage.type"));
    return switch (type) {
      case H2 -> new SqlStorageImpl(
          new H2Source(logger, plugin.getDataFolder().toPath()), logger, extractor);
      case SQLITE -> new SqlStorageImpl(new SqliteSource(logger, plugin.getDataFolder()
          .toPath()), logger, extractor);
      case POSTGRES ->
          new SqlStorageImpl(new HikariSource(StorageType.POSTGRES, sqlConfig.toHikari()), logger,
              extractor);
      default -> throw new IllegalStateException("Unexpected value: " + type);
    };
  }
}
