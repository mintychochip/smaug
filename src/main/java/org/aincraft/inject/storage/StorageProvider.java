package org.aincraft.inject.storage;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariConfig;
import java.util.logging.Logger;
import org.aincraft.config.PluginConfiguration;
import org.aincraft.storage.Extractor;
import org.aincraft.storage.IStorage;
import org.aincraft.storage.SqlStorageImpl;
import org.aincraft.storage.StorageType;
import org.aincraft.storage.flatfile.H2Source;
import org.aincraft.storage.flatfile.SqliteSource;
import org.bukkit.plugin.Plugin;

final class StorageProvider implements Provider<IStorage> {

  private final Logger logger;
  private final Plugin plugin;
  private final PluginConfiguration pluginConfiguration;
  private final HikariConfig hikariConfig;
  private final Extractor extractor;

  @Inject
  public StorageProvider(@Named("logger") Logger logger, Plugin plugin, @Named("main-configuration") PluginConfiguration pluginConfiguration,
      HikariConfig hikariConfig, Extractor extractor) {
    this.logger = logger;
    this.plugin = plugin;
    this.pluginConfiguration = pluginConfiguration;
    this.hikariConfig = hikariConfig;
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
      default -> throw new IllegalStateException("Unexpected value: " + type);
    };
  }
}
