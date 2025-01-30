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
import java.util.Set;
import java.util.logging.Logger;
import org.aincraft.config.PluginConfiguration;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.IKeyedItemFactory;
import org.aincraft.inject.IItemParser;
import org.aincraft.inject.IKeyFactory;
import org.aincraft.inject.implementation.RegistryImpl.ItemRegistryImpl;

final class ItemRegistryProvider implements Provider<IItemRegistry> {

  private final PluginConfiguration itemConfiguration;
  private final IItemParser parser;
  private final Logger logger;
  private final IKeyFactory keyFactory;
  private final IKeyedItemFactory keyedItemFactory;

  @Inject
  public ItemRegistryProvider(@Named("item") PluginConfiguration itemConfiguration,
      IItemParser parser, @Named("logger") Logger logger, IKeyFactory keyFactory,
      IKeyedItemFactory keyedItemFactory) {
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
