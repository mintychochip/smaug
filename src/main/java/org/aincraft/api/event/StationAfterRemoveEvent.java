package org.aincraft.api.event;

import org.aincraft.model.Station;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public final class StationAfterRemoveEvent extends StationRemoveEvent{

  public StationAfterRemoveEvent(Station station,
      @Nullable Player player,
      RemovalCause removalCause) {
    super(station, player, removalCause);
  }
}
