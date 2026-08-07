package org.aincraft.handler;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.aincraft.container.refining.RefiningPlayerStationProxy;
import org.aincraft.container.refining.RefiningService;
import org.aincraft.database.model.Station;
import org.aincraft.inject.implementation.viewmodel.RefiningGuiViewModel;
import org.bukkit.entity.Player;

/** Opens the item-free refining view after the integration access gate passes. */
public final class RefiningStationHandler implements StationHandler {

  private final Key key;
  private final RefiningGuiViewModel guiViewModel;
  private final RefiningService service;

  public RefiningStationHandler(Key key, RefiningGuiViewModel guiViewModel,
      RefiningService service) {
    this.key = key;
    this.guiViewModel = guiViewModel;
    this.service = service;
  }

  @Override
  public void handle(Context ctx) {
    if (!ctx.isRightClick()) {
      return;
    }
    ctx.cancel();
    Player player = ctx.getPlayer();
    Station station = ctx.getStation();
    if (!service.canOpen(player, station)) {
      player.sendMessage(Component.text("You cannot use this refining station."));
      return;
    }
    guiViewModel.open(new RefiningPlayerStationProxy(player, station));
  }

  @Override
  public Key key() {
    return key;
  }
}
