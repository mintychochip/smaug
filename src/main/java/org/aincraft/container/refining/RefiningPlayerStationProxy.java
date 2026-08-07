package org.aincraft.container.refining;

import java.util.Objects;
import java.util.UUID;
import org.aincraft.database.model.Station;
import org.bukkit.entity.Player;

/** Identifies one player's refining view at one placed station. */
public record RefiningPlayerStationProxy(Player player, Station station) {

  public RefiningPlayerStationProxy {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(station, "station");
    Objects.requireNonNull(station.id(), "station.id");
  }

  public BindingKey bindingKey() {
    return new BindingKey(player.getUniqueId(), station.id());
  }

  public record BindingKey(UUID playerId, UUID stationId) {
    public BindingKey {
      Objects.requireNonNull(playerId, "playerId");
      Objects.requireNonNull(stationId, "stationId");
    }
  }
}
