package org.aincraft.api.event;

import org.aincraft.database.model.StationInventory;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class StationInventoryEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private final StationInventory inventory;

  private boolean cancelled = false;
  public StationInventoryEvent(StationInventory inventory) {
    this.inventory = inventory;
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
