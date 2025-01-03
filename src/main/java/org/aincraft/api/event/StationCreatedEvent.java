package org.aincraft.api.event;

import org.aincraft.database.model.Station;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class StationCreatedEvent extends StationEvent {

  public StationCreatedEvent(Station table,
      @Nullable Player progenitor) {
    super(table, progenitor);
  }
}
