package org.aincraft.inject.implementation.viewmodel;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.kyori.adventure.key.Key;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.container.gui.AnvilGuiProxy;
import org.aincraft.inject.implementation.view.AnvilGuiProxyFactory;
import org.aincraft.listener.IStationService;
import org.bukkit.plugin.Plugin;

public class AnvilGuiControllerProvider implements
    Provider<IViewModelController<StationPlayerModelProxy, AnvilGuiProxy>> {

  private final IStationService stationService;
  private final Plugin plugin;

  @Inject
  public AnvilGuiControllerProvider(IStationService stationService, Plugin plugin) {
    this.stationService = stationService;
    this.plugin = plugin;
  }

  @Override
  public IViewModelController<StationPlayerModelProxy, AnvilGuiProxy> get() {
    AnvilGuiController controller = new AnvilGuiController(stationService, plugin);
    controller.register(Key.key("smaug:anvil"),new AnvilGuiViewModel(new AnvilGuiProxyFactory(stationService,plugin)));
    return controller;
  }
}
