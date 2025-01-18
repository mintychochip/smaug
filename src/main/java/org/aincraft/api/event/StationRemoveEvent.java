package org.aincraft.api.event;

import org.aincraft.database.model.Station;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.Nullable;

/**
 * This event is called when a station is supposed to be removed
 */
public final class StationRemoveEvent extends StationEvent implements Cancellable {

  private boolean cancelled = false;

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

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }
}
