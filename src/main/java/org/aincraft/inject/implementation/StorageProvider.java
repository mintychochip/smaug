///*
// *
// * Copyright (C) 2025 mintychochip
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// *
// */
//
//package org.aincraft.inject.implementation;
//
//import com.google.inject.Inject;
//import com.google.inject.Provider;
//import com.google.inject.name.Named;
//import java.util.logging.Logger;
//import org.aincraft.config.PluginConfiguration;
//import org.aincraft.database.storage.SqlConfig;
//import org.aincraft.database.storage.StorageType;
//import org.aincraft.database.storage.flatfile.H2Source;
//import org.aincraft.database.storage.flatfile.SqliteSource;
//import org.aincraft.database.storage.hikari.HikariSource;
//import org.bukkit.plugin.Plugin;
//
//final class StorageProvider implements Provider<IStorage> {
//
//  private final Logger logger;
//  private final Plugin plugin;
//  private final PluginConfiguration pluginConfiguration;
//  private final SqlConfig sqlConfig;
//  private final ResourceExtractor  extractor;
//
//  @Inject
//  public StorageProvider(@Named("logger") Logger logger, Plugin plugin,
//      PluginConfiguration pluginConfiguration,
//      SqlConfig sqlConfig, ResourceExtractor extractor) {
//    this.logger = logger;
//    this.plugin = plugin;
//    this.pluginConfiguration = pluginConfiguration;
//    this.sqlConfig = sqlConfig;
//    this.extractor = extractor;
//  }
//
//  @Override
//  public IStorage get() {
//    StorageType type = StorageType.fromIdentifier(
//        pluginConfiguration.getString("storage.type"));
//    return switch (type) {
//      case H2 -> new SqlStorageImpl(
//          new H2Source(logger, plugin.getDataFolder().toPath()), logger, extractor);
//      case SQLITE -> new SqlStorageImpl(new SqliteSource(logger, plugin.getDataFolder()
//          .toPath()), logger, extractor);
//      case POSTGRES ->
//          new SqlStorageImpl(new HikariSource(StorageType.POSTGRES, sqlConfig.toHikari()), logger,
//              extractor);
//      default -> throw new IllegalStateException("Unexpected value: " + type);
//    };
//  }
//}
