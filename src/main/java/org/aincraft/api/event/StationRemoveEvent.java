/*
 *
 * Copyright (C) 2025 mintychochip
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.aincraft.api.event;

import org.aincraft.database.model.test.IStation;
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

  public StationRemoveEvent(IStation station, @Nullable Player player,
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
