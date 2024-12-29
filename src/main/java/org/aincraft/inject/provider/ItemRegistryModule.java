package org.aincraft.inject.provider;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import org.aincraft.config.ConfigurationFactory;
import org.aincraft.config.PluginConfiguration;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.item.KeyedItem;
import org.bukkit.NamespacedKey;

public final class ItemRegistryModule extends AbstractModule {

  private final int version;
  private final NamespacedKey idKey;

  public ItemRegistryModule(int version, NamespacedKey idKey) {
    this.version = version;
    this.idKey = idKey;
  }

  @Override
  protected void configure() {
    this.bind(new TypeLiteral<IParser<KeyedItem, IItemRegistry>>() {
    }).to(ItemParserImpl.class).in(Singleton.class);
    this.bind(IAttributeParser.class).to(AttributeParserImpl.class).in(Singleton.class);
    this.bind(IItemRegistry.class).toProvider(ItemRegistryProvider.class).in(Singleton.class);
  }

  @Provides
  @Singleton
  public KeyedItemFactory provideItemFactory() {
    return new KeyedItemFactory(idKey,version);
  }
  @Provides
  @Singleton
  @Named("item-configuration")
  public PluginConfiguration provideConfiguration(ConfigurationFactory factory) {
    return factory.create("item.yml");
  }
}
