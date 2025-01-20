package org.aincraft.api.event;

import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationInventory;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class StationUpdateInventoryEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private final Station station;
  private final StationInventory inventory;

  private boolean cancelled = false;
  public StationUpdateInventoryEvent(Station station, StationInventory inventory) {
    this.station = station;
    this.inventory = inventory;
  }

  public Station getStation() {
    return station;
  }

  public StationInventory getInventory() {
    return inventory;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancel) {
    this.cancelled = cancel;
  }
}
