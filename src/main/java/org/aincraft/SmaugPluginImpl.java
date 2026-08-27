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
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.display.ViewModelController;
import org.aincraft.container.gui.GuiListener;
import org.aincraft.database.model.Station;
import org.aincraft.database.storage.IStorage;
import org.aincraft.api.refining.ProfessionGateway;
import org.aincraft.api.refining.RefiningStationAccess;
import org.aincraft.container.refining.RefiningIntegrationRegistry;
import org.aincraft.container.refining.RefiningService;
import org.aincraft.container.refining.RefiningStationType;
import org.aincraft.handler.AnvilStationHandler;
import org.aincraft.handler.RefiningStationHandler;
import org.aincraft.inject.IKeyFactory;
import org.aincraft.inject.IRecipeFetcher;
import org.aincraft.inject.implementation.viewmodel.AnvilGuiViewModel;
import org.aincraft.inject.implementation.viewmodel.AnvilGuiViewModel.AnvilGuiBinding;
import org.aincraft.inject.implementation.viewmodel.AnvilViewModel.AnvilDisplayBinding;
import org.aincraft.inject.implementation.viewmodel.ProgressBarViewModel;
import org.aincraft.inject.implementation.viewmodel.ProgressBarViewModel.BossBarBinding;
import org.aincraft.inject.implementation.viewmodel.RefiningGuiViewModel;
import org.aincraft.listener.PlayerListener;
import org.aincraft.listener.StationListener;
import org.aincraft.listener.StationModule;
import org.aincraft.listener.StationService;
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
  private final ViewModelController<Station, AnvilDisplayBinding> controller;
  private final ViewModelController<Station, BossBarBinding> bossBarController;
  private final ViewModelController<StationPlayerModelProxy, AnvilGuiBinding> guiController;
  private final StationService stationService;
  private final IItemRegistry itemRegistry;
  private final RefiningIntegrationRegistry refiningIntegrationRegistry;
  private final RefiningGuiViewModel refiningGuiViewModel;

  @Inject
  SmaugPluginImpl(Plugin bootstrap, IStorage storage,
      Injector injector, IKeyFactory keyFactory,
      IRecipeFetcher recipeFetcher,
      ViewModelController<Station, AnvilDisplayBinding> controller,
      ViewModelController<Station, BossBarBinding> bossBarController,
      ViewModelController<StationPlayerModelProxy, AnvilGuiBinding> guiController,
      StationService stationService,
      IItemRegistry itemRegistry,
      RefiningIntegrationRegistry refiningIntegrationRegistry,
      RefiningGuiViewModel refiningGuiViewModel) {
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
    this.refiningIntegrationRegistry = refiningIntegrationRegistry;
    this.refiningGuiViewModel = refiningGuiViewModel;
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
    Key anvilKey = new NamespacedKey(bootstrap, "anvil");
    AnvilGuiViewModel guiViewModel =
        (AnvilGuiViewModel) this.guiController.get(Key.key("smaug:anvil"));
    ProgressBarViewModel progressBarViewModel =
        (ProgressBarViewModel) this.bossBarController.get(Key.key("smaug:anvil"));
    handlers.put(anvilKey,
        new AnvilStationHandler(anvilKey, new NamespacedKey(bootstrap, "id"),
            guiViewModel, progressBarViewModel));
    RefiningService refiningService = injector.getInstance(RefiningService.class);
    for (RefiningStationType stationType : RefiningStationType.values()) {
      handlers.put(stationType.key(),
          new RefiningStationHandler(stationType.key(), this.refiningGuiViewModel, refiningService));
    }
  }

  private static void registerListeners(Listener[] listeners, Plugin plugin) {
    for (Listener listener : listeners) {
      Bukkit.getPluginManager().registerEvents(listener, plugin);
    }
  }

  void disable() {
    storage.close();
    // Drop projections and free world entities / hide tasks
    if (controller != null) {
      controller.clearAll();
    }
    if (bossBarController != null) {
      bossBarController.clearAll();
    }
    if (guiController != null) {
      guiController.clearAll();
    }
    refiningGuiViewModel.clearAll();
    handlers.clear();
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
  public StationService getStationService() {
    return stationService;
  }

  @Override
  public IItemRegistry getItemRegistry() {
    return itemRegistry;
  }

  @Override
  public void registerHandler(StationHandler handler) {
    handlers.put(handler.key(), handler);
  }

  @Override
  public void registerStationAccess(RefiningStationAccess access) {
    refiningIntegrationRegistry.registerStationAccess(access);
  }

  @Override
  public void registerProfessionGateway(ProfessionGateway gateway) {
    refiningIntegrationRegistry.registerProfessionGateway(gateway);
  }
}
