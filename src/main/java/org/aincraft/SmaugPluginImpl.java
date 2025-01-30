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

package org.aincraft;

import com.google.inject.Inject;
import com.google.inject.Injector;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.key.Key;
import org.aincraft.commands.IngredientCommand;
import org.aincraft.commands.SmithCommand;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.handler.StationHandler;
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.gui.GuiListener;
import org.aincraft.handler.AnvilStationHandler;
import org.aincraft.inject.IKeyFactory;
import org.aincraft.inject.IRecipeFetcher;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class SmaugPluginImpl implements ISmaugPlugin {

  private final Plugin bootstrap;
  private final Injector injector;
  private final IKeyFactory keyFactory;
  private final Map<Key, StationHandler<?>> handlers = new HashMap<>();
  private final IRecipeFetcher recipeFetcher;
  private final IItemRegistry itemRegistry;

  @Inject
  SmaugPluginImpl(Plugin bootstrap,
      Injector injector, IKeyFactory keyFactory,
      IRecipeFetcher recipeFetcher,
      IItemRegistry itemRegistry) {
    this.bootstrap = bootstrap;
    this.injector = injector;
    this.keyFactory = keyFactory;
    this.recipeFetcher = recipeFetcher;
    this.itemRegistry = itemRegistry;
  }

  void enable() {
    Smaug.setSmaug(this);
    Injector childInjector = injector.createChildInjector();
    registerListeners(new Listener[]{
         new GuiListener()}, bootstrap);
    if (bootstrap instanceof JavaPlugin jp) {
      jp.getCommand("smith").setExecutor(injector.getInstance(SmithCommand.class));
      jp.getCommand("test").setExecutor(injector.getInstance(IngredientCommand.class));
    }
//    handlers.put(new NamespacedKey(bootstrap, "anvil"),
//        new AnvilStationHandler(stationService, new NamespacedKey(bootstrap, "id")));

  }

  private static void registerListeners(Listener[] listeners, Plugin plugin) {
    for (Listener listener : listeners) {
      Bukkit.getPluginManager().registerEvents(listener, plugin);
    }
  }

  void disable() {
//    storage.close();
//    if (controller != null) {
//      controller.forEach(IViewModel::removeAll);
//    }
  }

  @Override
  public IRecipeFetcher getRecipeFetcher() {
    return recipeFetcher;
  }

  @Override
  public IKeyFactory getKeyFactory() {
    return keyFactory;
  }

  @Override
  public Plugin getPlugin() {
    return bootstrap;
  }

  @Override
  public IItemRegistry getItemRegistry() {
    return itemRegistry;
  }
}
