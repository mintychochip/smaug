package org.aincraft.storage;

public interface IStorage {
//
//  // Table-related methods
//  List<TableModel> getAllTables();
//  void deleteStation(Location location);
//  TableModel getStation(Location location);
//  boolean isTableType(Location location, String tableType);
//  List<TableModel> getTablesByType(@NotNull String tableType);
//  boolean hasInventory(@NotNull String tableId);
//  boolean hasRecipeProgress(@NotNull String tableId);
//  TableModel createTable(String tableType, Location location);
//  boolean hasStation(Location location);
//  TableInventoryModel createInventory(String tableId);
//  TableInventoryModel getStationInventory(String tableId);
//  void updateInventory(TableInventoryModel model);
//  void deleteRecipeProgress(String id);
//  TableRecipeProgressModel createRecipeProgress(String tableId, String recipeId);
//  TableRecipeProgressModel getRecipeProgress(String tableId);
//  void updateRecipeProgress(TableRecipeProgressModel model);
//
//  // StationUser-related methods
//  boolean hasStationUser(String playerId);
//  StationUser createStationUser(String playerId, String playerName);
//  StationUser getStationUser(String playerId);
//  void updateStationUser(StationUser user);
//  void deleteStationUser(String playerId);

  // Utility Methods
  void close();
}
