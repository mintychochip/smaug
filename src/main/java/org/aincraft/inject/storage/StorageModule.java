package org.aincraft.inject.storage;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import org.aincraft.database.storage.Extractor;
import org.aincraft.database.storage.Extractor.ResourceExtractor;
import org.aincraft.database.storage.IStorage;

public final class StorageModule extends AbstractModule {

  @Override
  protected void configure() {
    this.bind(HikariConfig.class).toProvider(HikariConfigProvider.class).in(Singleton.class);
    this.bind(Extractor.class).to(ResourceExtractor.class).in(Singleton.class);
    this.bind(IStorage.class).toProvider(StorageProvider.class).in(Singleton.class);
  }
}
