package org.aincraft.inject.implementation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.aincraft.api.event.SmaugInventoryEvent;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationInventory;
import org.aincraft.database.model.StationRecipeProgress;
import org.aincraft.database.model.StationUser;
import org.aincraft.database.storage.IStorage;
import org.aincraft.listener.IStationService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

@Singleton
final class StationServiceImpl implements IStationService {

  private final IStorage storage;
  private final Plugin plugin;
  //merge these two caches
  private static final Cache<UUID, Station> station2Cache;
  private static final Cache<Location, Station> stationCache;
  private static final Cache<Player, StationUser> userCache;
  private static final Cache<UUID, StationRecipeProgress> recipeCache;
  private static final Cache<UUID, StationInventory> inventoryCache;

  static {
    station2Cache = createCache();
    stationCache = createCache();
    userCache = createCache();
    recipeCache = createCache();
    inventoryCache = createCache();
  }

  private static <K, V> Cache<K, V> createCache() {
    return Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(1)).build();
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

  @Override
  public StationRecipeProgress createRecipeProgress(UUID stationId, String recipeKey) {
    StationRecipeProgress recipeProgress = storage.createRecipeProgress(stationId.toString(),
        recipeKey);
    recipeCache.put(stationId, recipeProgress);
    return recipeProgress;
  }

  @Override
  public StationRecipeProgress getRecipeProgress(UUID stationId) {
    return recipeCache.get(stationId,
        k -> storage.getRecipeProgress(stationId.toString()));
  }

  @Override
  public void deleteRecipeProgress(UUID stationId) {
    storage.deleteRecipeProgress(stationId.toString());
    recipeCache.invalidate(stationId);
  }

  @Override
  public boolean hasRecipeProgress(UUID stationId) {
    return this.getRecipeProgress(stationId) != null;
  }

  @Override
  public boolean updateRecipeProgress(StationRecipeProgress progress) {
    return storage.updateRecipeProgress(progress);
  }

  @Override
  public boolean updateRecipeProgress(UUID stationId,
      Consumer<StationRecipeProgress> progressConsumer) {
    StationRecipeProgress recipeProgress = this.getRecipeProgress(stationId);
    progressConsumer.accept(recipeProgress);
    return storage.updateRecipeProgress(recipeProgress);
  }

  @Override
  public StationInventory createInventory(UUID stationId, int inventoryLimit) {
    StationInventory inventory = storage.createInventory(stationId.toString(), inventoryLimit);
    inventoryCache.put(stationId, inventory);
    return inventory;
  }

  @Override
  public StationInventory getInventory(UUID stationId) {
    return inventoryCache.get(stationId, k -> storage.getInventory(k.toString()));
  }

  @Override
  public boolean hasInventory(UUID stationId) {
    if (inventoryCache.getIfPresent(stationId) != null) {
      return true;
    }
    return storage.hasInventory(stationId.toString());
  }

  @Override
  public boolean updateInventory(StationInventory inventory) {
    boolean b = storage.updateInventory(inventory);
    if(b) {
      inventoryCache.put(inventory.getStationId(), inventory);
    }
    return b;
  }

  @Override
  public void updateInventoryAsync(StationInventory inventory, Consumer<Boolean> callback) {
    CompletableFuture.supplyAsync(() -> this.updateInventory(inventory))
        .thenAcceptAsync(callback);
  }

  @Override
  public boolean updateInventory(UUID stationId, Consumer<StationInventory> inventoryConsumer) {
    StationInventory inventory = this.getInventory(stationId);
    inventoryConsumer.accept(inventory);
    return this.updateInventory(inventory);
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
