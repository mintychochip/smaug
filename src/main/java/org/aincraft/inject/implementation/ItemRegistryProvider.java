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
