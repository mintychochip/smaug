package org.aincraft.inject.implementation.view;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.key.Key;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.container.display.IViewModel;
import org.aincraft.listener.IStationService;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class ViewModelControllerProvider implements Provider<IViewModelController> {

  private final IStationService stationService;
  private final Plugin plugin;

  @Inject
  public ViewModelControllerProvider(IStationService stationService, Plugin plugin) {
    this.stationService = stationService;
    this.plugin = plugin;
  }

  @Override
  public IViewModelController get() {
    Map<Key, IViewModel> viewModels = new HashMap<>();
    ViewModelControllerImpl controller = new ViewModelControllerImpl(viewModels,
        stationService);
    controller.register(new NamespacedKey(plugin, "anvil"), new AnvilViewModel());
    return controller;
  }
}
