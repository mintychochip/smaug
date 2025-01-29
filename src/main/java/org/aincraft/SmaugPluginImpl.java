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

package org.aincraft;

import com.google.inject.Inject;
import com.google.inject.Injector;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import org.aincraft.commands.IngredientCommand;
import org.aincraft.commands.SmithCommand;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.StationHandler;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.gui.AnvilGuiProxy;
import org.aincraft.container.display.AnvilItemDisplayView;
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.container.gui.GuiListener;
import org.aincraft.database.model.Station;
import org.aincraft.database.storage.IStorage;
import org.aincraft.handler.AnvilStationHandler;
import org.aincraft.inject.IKeyFactory;
import org.aincraft.inject.IRecipeFetcher;
import org.aincraft.listener.IStationService;
import org.aincraft.listener.PlayerListener;
import org.aincraft.listener.StationListener;
import org.aincraft.listener.StationModule;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class SmaugPluginImpl implements ISmaugPlugin {

  private final Plugin bootstrap;
  private final IStorage storage;
  private final Injector injector;
  private final IKeyFactory keyFactory;
  private final Map<Key, StationHandler> handlers = new HashMap<>();
  private final IRecipeFetcher recipeFetcher;
  private final IViewModelController<Station, AnvilItemDisplayView> controller;
  private final IViewModelController<Station, BossBar> bossBarController;
  private final IViewModelController<StationPlayerModelProxy, AnvilGuiProxy> guiController;
  private final IStationService stationService;
  private final IItemRegistry itemRegistry;

  @Inject
  SmaugPluginImpl(Plugin bootstrap, IStorage storage,
      Injector injector, IKeyFactory keyFactory,
      IRecipeFetcher recipeFetcher, IViewModelController<Station, AnvilItemDisplayView> controller,
      IViewModelController<Station, BossBar> bossBarController,
      IViewModelController<StationPlayerModelProxy, AnvilGuiProxy> guiController,
      IStationService stationService,
      IItemRegistry itemRegistry) {
    this.bootstrap = bootstrap;
    this.storage = storage;
    this.injector = injector;
    this.keyFactory = keyFactory;
    this.recipeFetcher = recipeFetcher;
    this.controller = controller;
    this.bossBarController = bossBarController;
    this.guiController = guiController;
    this.stationService = stationService;
    this.itemRegistry = itemRegistry;
  }

  void enable() {
    Smaug.setSmaug(this);
    Injector childInjector = injector.createChildInjector(new StationModule(handlers));
    registerListeners(new Listener[]{childInjector.getInstance(StationListener.class),
        injector.getInstance(PlayerListener.class), new GuiListener(),
        controller, bossBarController, guiController}, bootstrap);
    if (bootstrap instanceof JavaPlugin jp) {
      jp.getCommand("smith").setExecutor(injector.getInstance(SmithCommand.class));
      jp.getCommand("test").setExecutor(injector.getInstance(IngredientCommand.class));
    }
    handlers.put(new NamespacedKey(bootstrap, "anvil"),
        new AnvilStationHandler(stationService, new NamespacedKey(bootstrap, "id"),
            this.guiController.get(Key.key("smaug:anvil")), this.bossBarController.get(Key.key("smaug:anvil"))));

  }

  private static void registerListeners(Listener[] listeners, Plugin plugin) {
    for (Listener listener : listeners) {
      Bukkit.getPluginManager().registerEvents(listener, plugin);
    }
  }

  void disable() {
    storage.close();
    if (controller != null) {
      controller.forEach(IViewModel::removeAll);
    }
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
  public IStationService getStationService() {
    return stationService;
  }

  @Override
  public IItemRegistry getItemRegistry() {
    return itemRegistry;
  }
}
