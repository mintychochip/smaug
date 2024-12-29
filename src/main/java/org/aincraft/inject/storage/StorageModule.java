package org.aincraft.inject.storage;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariConfig;
import java.util.logging.Logger;
import org.aincraft.config.PluginConfiguration;
import org.aincraft.storage.Extractor;
import org.aincraft.storage.Extractor.ResourceExtractor;
import org.aincraft.storage.IStorage;
import org.bukkit.plugin.Plugin;

public final class StorageModule extends AbstractModule {

  @Override
  protected void configure() {
    this.bind(HikariConfig.class).toProvider(HikariConfigProvider.class).in(Singleton.class);
    this.bind(Extractor.class).to(ResourceExtractor.class).in(Singleton.class);
    this.bind(IStorage.class).toProvider(StorageProvider.class).in(Singleton.class);
  }
}
