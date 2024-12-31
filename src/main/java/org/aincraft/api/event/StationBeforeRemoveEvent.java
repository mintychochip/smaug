package org.aincraft.api.event;

import org.aincraft.model.Station;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public final class StationBeforeRemoveEvent extends StationRemoveEvent {

  //TODO: Can include the inventory of the station before removal probably some race condition happening
  public StationBeforeRemoveEvent(Station station, @Nullable Player player,
      RemovalCause removalCause) {
    super(station, player, removalCause);
  }
}
