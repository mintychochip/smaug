package org.aincraft.api.event;

import org.aincraft.database.model.Station;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class StationEvent extends Event {

  private static HandlerList handlers = new HandlerList();

  private final Station station;

  @Nullable
  private final Player player;

  public StationEvent(Station station, @Nullable Player player) {
    this.station = station;
    this.player = player;
  }

  public @Nullable Player getPlayer() {
    return player;
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
