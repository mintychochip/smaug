package org.aincraft.inject.implementation.view;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.key.Key;
import org.aincraft.container.display.AnvilItemDisplayView;
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.database.model.Station;
import org.aincraft.listener.IStationService;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class ItemDisplayControllerProvider implements Provider<IViewModelController<Station, AnvilItemDisplayView>> {

  private final IStationService stationService;
  private final Plugin plugin;

  @Inject
  public ItemDisplayControllerProvider(IStationService stationService, Plugin plugin) {
    this.stationService = stationService;
    this.plugin = plugin;
  }

  @Override
  public IViewModelController<Station, AnvilItemDisplayView> get() {
    Map<Key, IViewModel<Station, AnvilItemDisplayView>> viewModels = new HashMap<>();
    ItemDisplayControllerImpl controller = new ItemDisplayControllerImpl(viewModels,
        stationService, plugin);
    controller.register(new NamespacedKey(plugin, "anvil"), new AnvilViewModel());
    return controller;
  }
}
