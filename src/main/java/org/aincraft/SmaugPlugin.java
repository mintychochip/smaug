package org.aincraft;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.aincraft.commands.IngredientCommand;
import org.aincraft.commands.SmithCommand;
import org.aincraft.container.IRecipeFetcher;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.StationHandler;
import org.aincraft.container.display.AnvilViewModel;
import org.aincraft.container.gui.RecipeMenuListener;
import org.aincraft.container.item.IKeyedItemFactory;
import org.aincraft.database.storage.IStorage;
import org.aincraft.handler.AnvilStationHandler;
import org.aincraft.listener.PlayerListener;
import org.aincraft.listener.StationListener;
import org.aincraft.listener.StationModule;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class SmaugPlugin implements Smaug {

  private final Plugin bootstrap;
  private final IStorage storage;
  private final IItemRegistry itemRegistry;
  private final Injector injector;
  private final Map<NamespacedKey, StationHandler> handlers = new HashMap<>();
  private final NamespacedKey idKey;
  private final IKeyedItemFactory keyedItemFactory;
  private final AnvilViewModel viewModel = new AnvilViewModel();
  private final IRecipeFetcher fetcher;

  @Inject
  SmaugPlugin(Plugin bootstrap, IStorage storage, IItemRegistry itemRegistry,
      Injector injector, @Named("id") NamespacedKey idKey, IKeyedItemFactory keyedItemFactory,
      IRecipeFetcher fetcher) {
    this.bootstrap = bootstrap;
    this.storage = storage;
    this.itemRegistry = itemRegistry;
    this.injector = injector;
    this.idKey = idKey;
    this.keyedItemFactory = keyedItemFactory;
    this.fetcher = fetcher;
  }

  void enable() {
    Injector childInjector = injector.createChildInjector(new StationModule(handlers));
    Bukkit.getPluginManager()
        .registerEvents(childInjector.getInstance(StationListener.class), bootstrap);
    Bukkit.getPluginManager().registerEvents(injector.getInstance(PlayerListener.class), bootstrap);
    Bukkit.getPluginManager()
        .registerEvents(injector.getInstance(RecipeMenuListener.class), bootstrap);
    if (bootstrap instanceof JavaPlugin jp) {
      jp.getCommand("smith").setExecutor(injector.getInstance(SmithCommand.class));
      jp.getCommand("test").setExecutor(injector.getInstance(IngredientCommand.class));
    }
    handlers.put(new NamespacedKey(bootstrap, "anvil"),
        injector.getInstance(AnvilStationHandler.class));
  }

  void disable() {
    storage.close();
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
