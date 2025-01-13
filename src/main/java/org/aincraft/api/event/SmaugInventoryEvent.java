package org.aincraft.api.event;

import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SmaugInventoryEvent extends Event {

  private static HandlerList handlers = new HandlerList();

  private final StationInventory inventory;

  public SmaugInventoryEvent(StationInventory inventory) {
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

}
