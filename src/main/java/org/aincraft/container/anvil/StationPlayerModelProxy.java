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

package org.aincraft.container.anvil;

import java.util.Objects;
import java.util.UUID;
import org.aincraft.database.model.Station;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Player viewing a station (GUI is per player × station).
 * Identity for binding maps is {@link #bindingKey()}.
 */
public record StationPlayerModelProxy(@NotNull Player player, @NotNull Station station) {

  public StationPlayerModelProxy {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(station, "station");
  }

  /**
   * Stable map key: player UUID + station UUID.
   */
  public @NotNull Object bindingKey() {
    return new BindingKey(player.getUniqueId(), station.id());
  }

  private record BindingKey(UUID playerId, UUID stationId) {}
}
