package org.aincraft.inject.implementation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationUser;
import org.aincraft.database.storage.IStorage;
import org.aincraft.listener.IStationService;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

@Singleton
final class StationServiceImpl implements IStationService {

  private final IStorage storage;
  private final Plugin plugin;
  //merge these two caches
  private final Cache<UUID, Station> station2Cache;
  private final Cache<Location, Station> stationCache;
  private final Cache<Player, StationUser> userCache;

  private static <K, V> Cache<K, V> createCache() {
    return Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(1)).build();
  }

  @Inject
  public StationServiceImpl(Plugin plugin, IStorage storage) {
    this.plugin = plugin;
    this.storage = storage;
    station2Cache = createCache();
    stationCache = createCache();
    userCache = createCache();
  }

  @Override
  public List<Station> getAllStations() {
    List<Station> stations = storage.getAllStations();
    for (Station station : stations) {
      station2Cache.put(station.id(), station);
      stationCache.put(station.blockLocation(), station);
    }
    return stations;
  }

  @Override
  public void updateStation(Station station) {
    stationCache.put(station.blockLocation(), station);
    station2Cache.put(station.id(), station);
    storage.updateStation(station);
  }

  @Override
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

  @Override
  public Station getStation(UUID stationId) {
    return station2Cache.get(stationId, k -> storage.getStation(stationId.toString()));
  }

  @Override
  public void deleteStation(Location location) {
    World world = location.getWorld();
    if (world == null) {
      return;
    }
    storage.deleteStation(world.getName(), location.getBlockX(), location.getBlockY(),
        location.getBlockZ());
    stationCache.invalidate(location);
  }

  @Override
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

  @Override
  public boolean hasStation(Location location) {
    return this.getStation(location) != null;
  }

  @Override
  public boolean hasStationUser(Player player) {
    return this.getStationUser(player) != null;
  }

  @Override
  public StationUser createStationUser(Player player) {
    StationUser user = storage.createStationUser(player.getUniqueId().toString(),
        player.getName());
    userCache.put(player, user);
    return user;
  }

  @Override
  public StationUser getStationUser(Player player) {
    return userCache.get(player,
        k -> storage.getStationUser(k.getUniqueId().toString()));
  }

  @Override
  public boolean updateStationUser(Player player) {
    StationUser user = this.getStationUser(player);
    if (user == null) {
      return false;
    }
    return storage.updateStationUser(user);
  }
}
