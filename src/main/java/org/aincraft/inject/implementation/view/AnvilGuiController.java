package org.aincraft.inject.implementation.view;

import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.display.IViewModel;
import org.aincraft.database.model.Station;
import org.aincraft.inject.implementation.controller.AbstractViewModelController;
import org.aincraft.listener.IStationService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

public class AnvilGuiController extends
    AbstractViewModelController<StationPlayerModelProxy, AnvilGuiProxy> {


  private final Plugin plugin;
  private final IStationService stationService;

  public AnvilGuiController(IStationService stationService, Plugin plugin) {
    this.stationService = stationService;
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleUpdate(final StationUpdateEvent event) {
    if (event.isCancelled()) {
      return;
    }
    Station model = event.getModel();
    Player player = event.getViewer();
    StationPlayerModelProxy proxy = new StationPlayerModelProxy(player, model);
    Bukkit.broadcastMessage(proxy.toString());
    IViewModel<StationPlayerModelProxy, AnvilGuiProxy> viewModel = this.get(model.stationKey());
    if (!viewModel.isBound(proxy)) {
      viewModel.bind(proxy,
          new AnvilGuiProxyFactory(stationService, plugin).create(proxy));
    }
    viewModel.update(new StationPlayerModelProxy(player, model));
  }
}
