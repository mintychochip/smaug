package org.aincraft.api.refining;

import org.aincraft.database.model.Station;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface RefiningStationAccess {

  RefiningStationAccessResult check(Player player, Station station);
}
