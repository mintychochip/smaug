package org.aincraft.api.event;

import org.aincraft.model.Station;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public abstract sealed class StationRemoveEvent extends StationEvent permits
    StationAfterRemoveEvent, StationBeforeRemoveEvent {

  public enum RemovalCause {
    PLAYER,
    EXPLOSION
  }

  private final RemovalCause removalCause;

  public StationRemoveEvent(Station station, @Nullable Player player,
      RemovalCause removalCause) {
    super(station, player);
    this.removalCause = removalCause;
  }

  public RemovalCause getRemovalCause() {
    return removalCause;
  }
}
