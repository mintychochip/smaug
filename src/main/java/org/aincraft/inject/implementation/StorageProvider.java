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
import com.google.inject.name.Named;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import org.aincraft.config.PluginConfiguration;
import org.aincraft.database.storage.IConnectionSource;
import org.aincraft.database.storage.IStorage;
import org.aincraft.database.storage.SqlConfig;
import org.aincraft.database.storage.SqlExecutor;
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
  private final ResourceExtractor extractor;

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
    IConnectionSource source = switch (type) {
      case H2 -> new H2Source(logger, plugin.getDataFolder().toPath());
      case SQLITE -> new SqliteSource(logger, plugin.getDataFolder()
          .toPath());
      case POSTGRES -> new HikariSource(StorageType.POSTGRES, sqlConfig.toHikari());
      default -> throw new IllegalStateException("Unexpected value: " + type);
    };
    boolean setup = isSetup(source);
    if(!setup) {
      String[] tables = type.getSqlTables(extractor).toArray(new String[0]);
      try {
        new SqlExecutor(source).executeBulk(tables);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
      logger.info("Successfully added tables to the database");
    }
    return new SqlStorageImpl(source);
  }
  private boolean isSetup(IConnectionSource source) {
    String query = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_name LIKE '%smaug%'";
    if (source.getType() == StorageType.SQLITE) {
      query = "SELECT 1 FROM sqlite_master WHERE type='table' LIMIT 1";
    }
    try (Connection connection = source.getConnection()) {
      PreparedStatement ps = connection.prepareStatement(query);
      ResultSet rs = ps.executeQuery();
      return rs.next();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
