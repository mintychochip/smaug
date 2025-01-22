package org.aincraft.inject.implementation.gui;

import dev.triumphteam.gui.guis.BaseGui;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.kyori.adventure.key.Key;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.display.AnvilGuiProxy;
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.database.model.Station;
import org.aincraft.inject.implementation.controller.AbstractViewModelController;
import org.aincraft.inject.implementation.view.AnvilGuiProxyFactory;
import org.aincraft.listener.IStationService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class AnvilGuiController extends
    AbstractViewModelController<StationPlayerModelProxy, AnvilGuiProxy> {

  private final IStationService stationService;
  private final Plugin plugin;

  public AnvilGuiController(IStationService stationService, Plugin plugin) {
    this.stationService = stationService;
    this.plugin = plugin;
  }

  @EventHandler
  private void handleUpdate(final StationUpdateEvent event) {
    if (event.isCancelled()) {
      return;
    }
    Station model = event.getModel();
    Player player = event.getViewer();
    StationPlayerModelProxy proxy = new StationPlayerModelProxy(player, model);
    IViewModel<StationPlayerModelProxy, AnvilGuiProxy> viewModel = this.get(model.stationKey());
    if (!viewModel.isBound(proxy.hashCode())) {
      viewModel.bind(proxy, new AnvilGuiProxyFactory(stationService, plugin).create(model, player));
      return;
    }
    Bukkit.broadcastMessage("here");
    viewModel.update(new StationPlayerModelProxy(player, model));
  }


}
