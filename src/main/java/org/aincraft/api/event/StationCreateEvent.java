package org.aincraft.api.event;

import org.aincraft.model.Station;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.Nullable;

public class StationCreateEvent extends StationEvent {

  public StationCreateEvent(Station table,
      @Nullable Player progenitor) {
    super(table, progenitor);
  }
}
