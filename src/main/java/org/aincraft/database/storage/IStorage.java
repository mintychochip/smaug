package org.aincraft.database.storage;

import java.util.List;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationUser;

public interface IStorage {

  //  // Table-related methods
  List<Station> getAllStations();

  void updateStation(Station model);

  Station createStation(String stationKey, String worldName, int x, int y, int z);

  void deleteStation(String worldName, int x, int y, int z);

  Station getStation(String worldName, int x, int y, int z);

  Station getStation(String stationId);

  boolean hasStationUser(String playerId);

  StationUser createStationUser(String playerId, String playerName);

  StationUser getStationUser(String playerId);

  boolean updateStationUser(StationUser user);

  void close();
}
