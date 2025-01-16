package org.aincraft.database.storage;

import java.util.List;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationInventory;
import org.aincraft.database.model.StationRecipeProgress;
import org.aincraft.database.model.StationUser;

public interface IStorage {

  //  // Table-related methods
  List<Station> getAllStations();

  List<StationInventory> getAllInventories();

  Station createStation(String stationKey, String worldName, int x, int y, int z);

  void deleteStation(String worldName, int x, int y, int z);

  Station getStation(String worldName, int x, int y, int z);

  Station getStation(String stationId);

  boolean hasStation(String worldName, int x, int y, int z);

  boolean hasStationUser(String playerId);

  StationUser createStationUser(String playerId, String playerName);

  StationUser getStationUser(String playerId);

  boolean updateStationUser(StationUser user);

  StationRecipeProgress createRecipeProgress(String stationId, String recipeKey);

  StationRecipeProgress getRecipeProgress(String stationId);

  void deleteRecipeProgress(String stationId);

  boolean hasRecipeProgress(String stationId);

  boolean updateRecipeProgress(StationRecipeProgress progress);

  StationInventory createInventory(String stationId, int inventoryLimit);

  StationInventory getInventory(String stationId);

  boolean hasInventory(String stationId);

  boolean updateInventory(StationInventory inventory);
//
//  StationUserPermission createUserPermission(String stationId, String playerId, int permission);
//
//  StationUserPermission getUserPermission(String stationId, String playerId);
//
//  boolean updateUserPermission(StationUserPermission permission);
//
//  void deleteUserPermission(String stationId, String playerId);

  void close();
}
