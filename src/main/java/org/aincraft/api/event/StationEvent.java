package org.aincraft.api.event;

import org.aincraft.model.Station;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class StationEvent extends Event {

  private static HandlerList handlers = new HandlerList();

  private final Station station;

  @Nullable
  private final Player progenitor;

  public StationEvent(Station station, @Nullable Player progenitor) {
    this.station = station;
    this.progenitor = progenitor;
  }

  public @Nullable Player getProgenitor() {
    return progenitor;
  }

  public Station getStation() {
    return station;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
