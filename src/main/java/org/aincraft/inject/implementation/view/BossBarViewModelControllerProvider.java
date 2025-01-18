package org.aincraft.inject.implementation.view;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.kyori.adventure.bossbar.BossBar;
import org.aincraft.container.IRecipeFetcher;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.database.model.RecipeProgress;
import org.aincraft.listener.IStationService;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public class BossBarViewModelControllerProvider implements
    Provider<IViewModelController<RecipeProgress, BossBar>> {

  private final IStationService stationService;
  private final IRecipeFetcher recipeFetcher;
  private final Plugin plugin;

  @Inject
  public BossBarViewModelControllerProvider(IStationService stationService,
      IRecipeFetcher recipeFetcher, Plugin plugin) {
    this.stationService = stationService;
    this.recipeFetcher = recipeFetcher;
    this.plugin = plugin;
  }

  @Override
  public IViewModelController<RecipeProgress, BossBar> get() {
    ProgressBarControllerImpl controller = new ProgressBarControllerImpl(stationService,
        recipeFetcher, plugin);
    controller.register(new NamespacedKey(plugin, "anvil"), new BossBarModel(plugin));
    return controller;
  }
}
