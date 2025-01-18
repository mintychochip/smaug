package org.aincraft.listener;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.aincraft.database.model.RecipeProgress;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationInventory;
import org.aincraft.database.model.StationUser;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public interface IStationService {

  List<Station> getAllStations();
  List<StationInventory> getAllInventories();
  Station createStation(NamespacedKey stationKey, Location location);

  void deleteStation(Location location);

  Station getStation(Location location);

  Station getStation(UUID stationId);

  boolean hasStation(Location location);

  boolean hasStationUser(Player player);

  StationUser createStationUser(Player player);

  StationUser getStationUser(Player player);

  boolean updateStationUser(Player player);

  RecipeProgress createRecipeProgress(UUID stationId, String recipeKey);

  RecipeProgress getRecipeProgress(UUID stationId);

  void deleteRecipeProgress(UUID stationId);

  boolean hasRecipeProgress(UUID stationId);

  boolean updateRecipeProgress(RecipeProgress progress);

  boolean updateRecipeProgress(UUID stationId, Consumer<RecipeProgress> progressConsumer);

  StationInventory createInventory(UUID stationId, int inventoryLimit);

  StationInventory getInventory(UUID stationId);

  boolean hasInventory(UUID stationId);

  boolean updateInventory(StationInventory inventory);

  void updateInventoryAsync(StationInventory inventory, Consumer<Boolean> callback);

  boolean updateInventory(UUID stationId, Consumer<StationInventory> inventoryConsumer);
}
