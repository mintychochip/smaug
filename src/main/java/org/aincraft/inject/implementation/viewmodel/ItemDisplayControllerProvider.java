package org.aincraft.inject.implementation.viewmodel;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.aincraft.container.display.AnvilItemDisplayView;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.database.model.Station;
import org.aincraft.listener.IStationService;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class ItemDisplayControllerProvider implements
    Provider<IViewModelController<Station, AnvilItemDisplayView>> {

  private final IStationService stationService;
  private final Plugin plugin;

  @Inject
  public ItemDisplayControllerProvider(IStationService stationService, Plugin plugin) {
    this.stationService = stationService;
    this.plugin = plugin;
  }

  @Override
  public IViewModelController<Station, AnvilItemDisplayView> get() {
    ItemDisplayControllerImpl controller = new ItemDisplayControllerImpl();
    controller.register(new NamespacedKey(plugin, "anvil"), new AnvilViewModel());
    return controller;
  }
}
