package org.aincraft.inject.implementation.view;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.kyori.adventure.bossbar.BossBar;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.database.model.Station;
import org.aincraft.inject.IRecipeFetcher;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public class BossBarViewModelControllerProvider implements
    Provider<IViewModelController<Station, BossBar>> {

  private final IRecipeFetcher recipeFetcher;
  private final Plugin plugin;

  @Inject
  public BossBarViewModelControllerProvider(
      IRecipeFetcher recipeFetcher, Plugin plugin) {
    this.recipeFetcher = recipeFetcher;
    this.plugin = plugin;
  }

  @Override
  public IViewModelController<Station, BossBar> get() {
    ProgressBarControllerImpl controller = new ProgressBarControllerImpl();
    controller.register(new NamespacedKey(plugin, "anvil"), new BossBarViewModel());
    return controller;
  }
}
