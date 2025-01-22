package org.aincraft.container.anvil;

import org.aincraft.database.model.Station;
import org.bukkit.entity.Player;

public record StationPlayerModelProxy(Player player, Station station) {

  @Override
  public int hashCode() {
    return player.hashCode() + station.id().hashCode();
  }
}
