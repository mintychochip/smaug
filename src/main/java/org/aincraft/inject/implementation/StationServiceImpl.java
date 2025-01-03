package org.aincraft.inject.implementation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.Duration;
import java.util.UUID;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationRecipeProgress;
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

  private static final Duration DEFAULT_EXPIRE = Duration.ofHours(1);
  private final Cache<Location, Station> stationCache = Caffeine.newBuilder().expireAfterWrite(
      DEFAULT_EXPIRE).build();
  private final Cache<Player, StationUser> userCache = Caffeine.newBuilder()
      .expireAfterWrite(DEFAULT_EXPIRE).build();
  private final Cache<StationRecipeKey, StationRecipeProgress> recipeCache = Caffeine.newBuilder()
      .expireAfterWrite(DEFAULT_EXPIRE).build();

  record StationRecipeKey(UUID stationId) {

    @Override
    public int hashCode() {
      return stationId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      StationRecipeKey other = (StationRecipeKey) obj;
      return stationId.equals(other.stationId());
    }
  }

  @Inject
  public StationServiceImpl(Plugin plugin, IStorage storage) {
    this.plugin = plugin;
    this.storage = storage;
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
    return stationCache.get(location, l -> {
      World world = location.getWorld();
      if (world == null) {
        return null;
      }
      return storage.getStation(world.getName(), location.getBlockX(), location.getBlockY(),
          location.getBlockZ());
    });
  }

  @Override
  public boolean hasStation(Location location) {
    World world = location.getWorld();
    if (world == null) {
      return false;
    }
    return stationCache.getIfPresent(location) == null && storage.hasStation(world.getName(),
        location.getBlockX(), location.getBlockY(),
        location.getBlockZ());
  }

  @Override
  public boolean hasStationUser(Player player) {
    return userCache.getIfPresent(player) == null && storage.hasStationUser(
        player.getUniqueId().toString());
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
        p -> storage.getStationUser(player.getUniqueId().toString()));
  }

  @Override
  public boolean updateStationUser(Player player) {
    StationUser user = this.getStationUser(player);
    if (user == null) {
      return false;
    }
    return storage.updateStationUser(user);
  }

  @Override
  public StationRecipeProgress createRecipeProgress(UUID stationId, NamespacedKey recipeKey) {
    StationRecipeKey stationKey = new StationRecipeKey(stationId);
    StationRecipeProgress recipeProgress = storage.createRecipeProgress(stationId.toString(),
        recipeKey.toString());
    recipeCache.put(stationKey, recipeProgress);
    return recipeProgress;
  }

  @Override
  public StationRecipeProgress getRecipeProgress(UUID stationId) {
    return recipeCache.get(new StationRecipeKey(stationId),
        k -> storage.getRecipeProgress(stationId.toString()));
  }

  @Override
  public void deleteRecipeProgress(UUID stationId) {
    storage.deleteRecipeProgress(stationId.toString());
    recipeCache.invalidate(new StationRecipeKey(stationId));
  }

  @Override
  public boolean hasRecipeProgress(UUID stationId) {
    return recipeCache.getIfPresent(new StationRecipeKey(stationId)) == null
        && storage.hasRecipeProgress(stationId.toString());
  }

  @Override
  public boolean updateRecipeProgress(UUID stationId) {
    StationRecipeProgress recipeProgress = storage.getRecipeProgress(stationId.toString());
    if(recipeProgress == null) {
      return false;
    }
    return storage.updateRecipeProgress(recipeProgress);
  }
//
//  public void createStation(String stationKey, Location location,
//      @Nullable Player player) {
//    CompletableFuture.supplyAsync(() -> storage.hasStation(location))
//        .thenAcceptAsync(exists -> {
//          if (exists) {
//            return;
//          }
//          CompletableFuture.supplyAsync(() -> storage.createStation(stationKey, location))
//              .thenAccept(station -> {
//                if (station != null) {
//                  new BukkitRunnable() {
//                    @Override
//                    public void run() {
//                      stationCache.put(location, station);
//                      Bukkit.getPluginManager()
//                          .callEvent(new StationCreatedEvent(station, player));
//                    }
//                  }.runTask(plugin);
//                }
//              });
//        });
//  }
//
//  public void deleteStation(Location location, @Nullable Player player,
//      RemovalCause removalCause) {
//    CompletableFuture.supplyAsync(() -> storage.getStation(location)).thenAcceptAsync(station -> {
//      if (station == null) {
//        return;
//      }
//      new BukkitRunnable() {
//        @Override
//        public void run() {
//          Bukkit.getPluginManager()
//              .callEvent(new StationBeforeRemoveEvent(station, player, removalCause));
//          CompletableFuture.runAsync(() -> {
//            storage.deleteStation(location);
//            stationCache.invalidate(location);
//            new BukkitRunnable() {
//              @Override
//              public void run() {
//                Bukkit.getPluginManager()
//                    .callEvent(new StationAfterRemoveEvent(station, player, removalCause));
//              }
//            }.runTask(plugin);
//          });
//        }
//      }.runTask(plugin);
//    });
//  }

}
