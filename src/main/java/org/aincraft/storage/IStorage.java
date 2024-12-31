package org.aincraft.storage;

import java.util.List;
import org.aincraft.model.Station;
import org.aincraft.model.StationUser;
import org.bukkit.Location;

public interface IStorage {

//  // Table-related methods
  List<Station> getAllStations();
  Station createStation(String stationKey, Location location);
  void deleteStation(Location location);
  Station getStation(Location location);
  boolean hasStation(Location location);
  boolean hasStationUser(String playerId);
  StationUser createStationUser(String playerId, String playerName);
  StationUser getStationUser(String playerId);
  boolean updateStationUser(StationUser user);
//  boolean isTableType(Location location, String tableType);
//  List<TableModel> getTablesByType(@NotNull String tableType);
//  boolean hasInventory(@NotNull String tableId);
//  boolean hasRecipeProgress(@NotNull String tableId);
//  TableInventoryModel createInventory(String tableId);
//  TableInventoryModel getStationInventory(String tableId);
//  void updateInventory(TableInventoryModel model);
//  void deleteRecipeProgress(String id);
//  TableRecipeProgressModel createRecipeProgress(String tableId, String recipeId);
//  TableRecipeProgressModel getRecipeProgress(String tableId);
//  void updateRecipeProgress(TableRecipeProgressModel model);
//


  // Utility Methods
  void close();
}
