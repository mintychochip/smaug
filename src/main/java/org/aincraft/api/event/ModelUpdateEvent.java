package org.aincraft.api.event;

import org.aincraft.database.model.RecipeProgress;
import org.aincraft.database.model.StationInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

abstract class ModelUpdateEvent<T> extends Event implements Cancellable {

  private static HandlerList handlers = new HandlerList();

  private final T model;

  private final Player player;

  private boolean cancelled = false;

  public ModelUpdateEvent(T t, Player player) {
    this.model = t;
    this.player = player;
  }

  public Player getViewer() {
    return player;
  }

  public T getModel() {
    return model;
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

  static final class InventoryUpdateEvent extends ModelUpdateEvent<StationInventory> {

    public InventoryUpdateEvent(StationInventory inventory, Player player) {
      super(inventory, player);
    }
  }
}
