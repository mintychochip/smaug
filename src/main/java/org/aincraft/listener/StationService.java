/*
 *
 * Copyright (C) 2025 mintychochip
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.aincraft.listener;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationUser;
import org.aincraft.database.storage.IStorage;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Station CRUD and cache. Single concrete service — no interface indirection.
 */
@Singleton
public final class StationService {

  private final IStorage storage;
  private final Plugin plugin;
  private final Cache<UUID, Station> station2Cache;
  private final Cache<Location, Station> stationCache;
  private final Cache<Player, StationUser> userCache;

  private static <K, V> Cache<K, V> createCache() {
    return Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(1)).build();
  }

  @Inject
  public StationService(Plugin plugin, IStorage storage) {
    this.plugin = plugin;
    this.storage = storage;
    station2Cache = createCache();
    stationCache = createCache();
    userCache = createCache();
  }

  public List<Station> getAllStations() {
    List<Station> stations = storage.getAllStations();
    for (Station station : stations) {
      station2Cache.put(station.id(), station);
      stationCache.put(station.blockLocation(), station);
    }
    return stations;
  }

  public void updateStation(Station station) {
    stationCache.put(station.blockLocation(), station);
    station2Cache.put(station.id(), station);
    storage.updateStation(station);
  }

  public Station createStation(NamespacedKey stationKey, Location location) {
    World world = location.getWorld();
    if (world == null) {
      return null;
    }
    Station station = storage.createStation(stationKey.toString(), world.getName(),
        location.getBlockX(),
        location.getBlockY(),
        location.getBlockZ());
    stationCache.put(location, station);
    return station;
  }

  public Station getStation(UUID stationId) {
    return station2Cache.get(stationId, k -> storage.getStation(stationId.toString()));
  }

  public void deleteStation(Location location) {
    World world = location.getWorld();
    if (world == null) {
      return;
    }
    storage.deleteStation(world.getName(), location.getBlockX(), location.getBlockY(),
        location.getBlockZ());
    stationCache.invalidate(location);
  }

  public Station getStation(Location location) {
    return stationCache.get(location, k -> {
      World world = k.getWorld();
      if (world == null) {
        return null;
      }
      return storage.getStation(world.getName(), k.getBlockX(), k.getBlockY(),
          k.getBlockZ());
    });
  }

  public boolean hasStation(Location location) {
    return this.getStation(location) != null;
  }

  public boolean hasStationUser(Player player) {
    return this.getStationUser(player) != null;
  }

  public StationUser createStationUser(Player player) {
    StationUser user = storage.createStationUser(player.getUniqueId().toString(),
        player.getName());
    userCache.put(player, user);
    return user;
  }

  public StationUser getStationUser(Player player) {
    return userCache.get(player,
        k -> storage.getStationUser(k.getUniqueId().toString()));
  }

  public boolean updateStationUser(Player player) {
    StationUser user = this.getStationUser(player);
    if (user == null) {
      return false;
    }
    return storage.updateStationUser(user);
  }
}
