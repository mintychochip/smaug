package org.aincraft.inject.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import java.util.Set;
import java.util.logging.Logger;
import org.aincraft.config.PluginConfiguration;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.inject.provider.RegistryImpl.ItemRegistryImpl;

final class ItemRegistryProvider implements Provider<IItemRegistry> {

  private final PluginConfiguration itemConfiguration;
  private final IItemParser parser;
  private final Logger logger;
  private final KeyFactory keyFactory;
  private final KeyedItemFactoryImpl keyedItemFactory;

  @Inject
  public ItemRegistryProvider(@Named("item") PluginConfiguration itemConfiguration,
      IItemParser parser, @Named("logger") Logger logger, KeyFactory keyFactory,
      KeyedItemFactoryImpl keyedItemFactory) {
    this.itemConfiguration = itemConfiguration;
    this.parser = parser;
    this.logger = logger;
    this.keyFactory = keyFactory;
    this.keyedItemFactory = keyedItemFactory;
  }

  @Override
  public IItemRegistry get() {
    ItemRegistryImpl registry = new ItemRegistryImpl(keyFactory, keyedItemFactory);
    Set<String> keys = itemConfiguration.getKeys(false);
    for (String itemKey : keys) {
      IKeyedItem item = parser.parse(itemConfiguration.getConfigurationSection(itemKey), registry);
      if(item != null) {
        registry.register(item);
      }
    }
    return registry;
  }
}
