package org.aincraft;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.kyori.adventure.key.Key;
import org.aincraft.commands.IngredientCommand;
import org.aincraft.commands.SmithCommand;
import org.aincraft.container.IRecipeFetcher;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.StationHandler;
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.container.gui.GuiListener;
import org.aincraft.container.item.IKeyedItemFactory;
import org.aincraft.database.model.Station;
import org.aincraft.database.storage.IStorage;
import org.aincraft.handler.AnvilStationHandler;
import org.aincraft.listener.IStationService;
import org.aincraft.listener.PlayerListener;
import org.aincraft.listener.StationListener;
import org.aincraft.listener.StationModule;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class SmaugPlugin implements Smaug {

  private final Plugin bootstrap;
  private final IStorage storage;
  private final IItemRegistry itemRegistry;
  private final Injector injector;
  private final Map<Key, StationHandler> handlers = new HashMap<>();
  private final NamespacedKey idKey;
  private final IKeyedItemFactory keyedItemFactory;
  private final IRecipeFetcher fetcher;
  private final IViewModelController controller;
  private final IStationService stationService;

  @Inject
  SmaugPlugin(Plugin bootstrap, IStorage storage, IItemRegistry itemRegistry,
      Injector injector, @Named("id") NamespacedKey idKey, IKeyedItemFactory keyedItemFactory,
      IRecipeFetcher fetcher, IViewModelController controller, IStationService stationService) {
    this.bootstrap = bootstrap;
    this.storage = storage;
    this.itemRegistry = itemRegistry;
    this.injector = injector;
    this.idKey = idKey;
    this.keyedItemFactory = keyedItemFactory;
    this.fetcher = fetcher;
    this.controller = controller;
    this.stationService = stationService;
  }

  void enable() {
    Instant now = Instant.now();
    Injector childInjector = injector.createChildInjector(new StationModule(handlers));
    Bukkit.getPluginManager()
        .registerEvents(childInjector.getInstance(StationListener.class), bootstrap);
    Bukkit.getPluginManager().registerEvents(injector.getInstance(PlayerListener.class), bootstrap);
    Bukkit.getPluginManager()
        .registerEvents(injector.getInstance(GuiListener.class), bootstrap);
    Bukkit.getPluginManager().registerEvents(controller,bootstrap);
    if (bootstrap instanceof JavaPlugin jp) {
      jp.getCommand("smith").setExecutor(injector.getInstance(SmithCommand.class));
      jp.getCommand("test").setExecutor(injector.getInstance(IngredientCommand.class));
    }
    handlers.put(new NamespacedKey(bootstrap, "anvil"),
        injector.getInstance(AnvilStationHandler.class));
//    CompletableFuture.runAsync(() -> stationService.getAllInventories());
//    CompletableFuture.supplyAsync(() -> stationService.getAllStations()).thenAcceptAsync(stations -> {
//      new BukkitRunnable() {
//        @Override
//        public void run() {
//          for(Station s : stations) {
//            controller.update(s.getId());
//          }
//        }
//      }.runTask(this.bootstrap);
//    });
  }

  void disable() {
    storage.close();
    if (controller != null) {
      controller.forEach(IViewModel::removeAll);
    }
  }

  @Override
  public @Nullable SmaugRecipe fetch(String recipeKey) {
    return fetcher.fetch(recipeKey);
  }

  @Override
  public @NotNull List<SmaugRecipe> all(@NotNull Predicate<SmaugRecipe> recipePredicate) {
    return fetcher.all(recipePredicate);
  }

}
