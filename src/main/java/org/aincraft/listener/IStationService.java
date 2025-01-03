package org.aincraft.listener;

import java.util.UUID;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationRecipeProgress;
import org.aincraft.database.model.StationUser;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public interface IStationService {

  Station createStation(NamespacedKey stationKey, Location location);

  void deleteStation(Location location);

  Station getStation(Location location);

  boolean hasStation(Location location);

  boolean hasStationUser(Player player);

  StationUser createStationUser(Player player);

  StationUser getStationUser(Player player);

  boolean updateStationUser(Player player);

  StationRecipeProgress createRecipeProgress(UUID stationId, NamespacedKey recipeKey);

  StationRecipeProgress getRecipeProgress(UUID stationId);

  void deleteRecipeProgress(UUID stationId);

  boolean hasRecipeProgress(UUID stationId);

  boolean updateRecipeProgress(UUID stationId);
}
