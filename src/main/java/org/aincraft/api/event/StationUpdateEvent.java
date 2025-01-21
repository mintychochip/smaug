package org.aincraft.api.event;

import org.aincraft.database.model.Station;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class StationUpdateEvent extends Event implements Cancellable {

  private static HandlerList handlers = new HandlerList();

  private final Station model;

  private final Player player;

  private boolean cancelled = false;

  public StationUpdateEvent(Station model, Player player) {
    this.model = model;
    this.player = player;
  }


  public Player getViewer() {
    return player;
  }

  public Station getModel() {
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
}
