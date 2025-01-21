package org.aincraft.listener;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationUser;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public interface IStationService {

  List<Station> getAllStations();
  Station createStation(NamespacedKey stationKey, Location location);

  void updateStation(Station station);
  void deleteStation(Location location);

  Station getStation(Location location);

  Station getStation(UUID stationId);

  boolean hasStation(Location location);

  boolean hasStationUser(Player player);

  StationUser createStationUser(Player player);

  StationUser getStationUser(Player player);

  boolean updateStationUser(Player player);
}
