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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.kyori.adventure.key.Key;
import org.aincraft.api.event.StationRemoveEvent;
import org.aincraft.api.event.StationRemoveEvent.RemovalCause;
import org.aincraft.database.model.test.IStation;
import org.aincraft.database.storage.IConnectionSource;
import org.aincraft.database.storage.SqlExecutor;
import org.aincraft.handler.StationHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class StationListener implements Listener {

  public static final Predicate<Block> IS_FALLING_BLOCK_TYPE = block -> {
    Material material = block.getType();
    String materialString = material.toString();
    return material == Material.RED_SAND ||
        material == Material.SAND ||
        material == Material.GRAVEL ||
        material == Material.POWDER_SNOW ||
        material == Material.ANVIL ||
        material == Material.CHIPPED_ANVIL ||
        material == Material.DAMAGED_ANVIL ||
        materialString.contains("CONCRETE_POWDER");
  };
  private final Map<Key, StationHandler<?>> handlers;
  private final Plugin plugin;
  private final NamespacedKey stationKey;
  private final Map<Key, IMutableStationService<?>> serviceMap;
  private final IConnectionSource connectionSource;

  @Inject
  public StationListener(Map<Key, StationHandler<?>> handlers,
      Plugin plugin,
      @Named("station") NamespacedKey stationKey, Map<Key, IMutableStationService<?>> serviceMap,
      IConnectionSource connectionSource) {
    this.handlers = handlers;
    this.plugin = plugin;
    this.stationKey = stationKey;
    this.serviceMap = serviceMap;
    this.connectionSource = connectionSource;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void onPlaceStation(final BlockPlaceEvent event) {
    ItemStack itemInHand = event.getItemInHand();
    ItemMeta itemMeta = itemInHand.getItemMeta();
    if (itemMeta == null) {
      return;
    }
    PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
    String keyString = pdc.getOrDefault(this.stationKey, PersistentDataType.STRING, "");
    if (keyString.isEmpty()) {
      return;
    }
    Block block = event.getBlockPlaced();
    Location blockLocation = block.getLocation();
    World world = blockLocation.getWorld();
    if (world == null) {
      return;
    }
    Block blockBelow = world.getBlockAt(blockLocation.clone().add(0, -1, 0));
    if (IS_FALLING_BLOCK_TYPE.test(block) && blockBelow.getType().isAir()) {
      event.setCancelled(true);
      return;
    }
    Player player = event.getPlayer();
    IMutableStationService<?> service = serviceMap.get(Key.key(keyString));
    if (service == null) {
      return;
    }
    service.createStation(Key.key(keyString), blockLocation);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void removeBlocksCheckForStation(final BlockBreakEvent event) {
    final KeyService keyService = new KeyService(new SqlExecutor(connectionSource));
    final IStation station = keyService.getStation(event.getBlock().getLocation());
    if (station == null) {
      return;
    }
    Player player = event.getPlayer();
    Bukkit.getPluginManager()
        .callEvent(new StationRemoveEvent(station, player, RemovalCause.PLAYER));
    for (int y = blockLocation.getBlockY() + 1; y < world.getMaxHeight(); y++) {
      Block blockAbove = world.getBlockAt(
          new Location(world, blockLocation.getBlockX(), y, blockLocation.getBlockZ()));
      if (!IS_FALLING_BLOCK_TYPE.test(blockAbove)) {
        break;
      }
      Bukkit.getPluginManager()
          .callEvent(new StationRemoveEvent(IStation, player, RemovalCause.PLAYER));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleRemoveStation(final StationRemoveEvent event) {
    if (event.isCancelled()) {
      return;
    }
    IStation station = event.getStation();
    CompletableFuture.runAsync(() -> stationService.deleteStation(station.blockLocation()));
    final World world = station.world();
    Meta<?> meta = station.getMeta();
    if(meta instanceof TrackableProgressMeta tm) {
      StationInventory stationInventory = tm.getInventory();
      final Location location = station.centerLocation();
      new BukkitRunnable() {
        @Override
        public void run() {
          List<ItemStack> contents = stationInventory.getContents();
          for (ItemStack content : contents) {
            if (content != null) {
              world.dropItemNaturally(location, content);
            }
          }
        }
      }.runTask(plugin);
    }
  }

  //  @EventHandler(priority = EventPriority.MONITOR)
//  private void handleUpdateStation(final StationUpdateEvent<?> event) {
//    if (event.isCancelled()) {
//      return;
//    }
//    CompletableFuture.runAsync(() -> stationService.updateStation(event.getStation()));
//  }
//
//  @EventHandler(priority = EventPriority.MONITOR)
//  private void handleInteract(final PlayerInteractEvent event) {
//    Block block = event.getClickedBlock();
//    if (block == null) {
//      return;
//    }
//    if (block.getType().isAir()) {
//      return;
//    }
//    Station<?> station = stationService.getStation(block.getLocation());
//    if (station == null) {
//      return;
//    }
//    StationHandler<?> handler = handlers.get(station.stationKey());
//    if (handler == null) {
//      return;
//    }
//    EquipmentSlot hand = event.getHand();
//    if (hand == EquipmentSlot.OFF_HAND) {
//      return;
//    }
//   /// handler.handle(Context.create(station,event));
//  }

  static final class KeyService {

    private static final String GET_KEY_BY_LOCATION = "SELECT id,station_key FROM stations WHERE world_name=? AND x=? AND y=? AND z=?";

    private final SqlExecutor executor;

    KeyService(SqlExecutor executor) {
      this.executor = executor;
    }

    @Nullable
    public IStation getStation(Location location) {
      final String worldName = location.getWorld().getName();
      final int x = location.getBlockX();
      final int y = location.getBlockY();
      final int z = location.getBlockZ();
      Preconditions.checkNotNull(location);
      return executor.queryRow(scanner -> {
        try {
          String idString = scanner.getString("id");
          String keyString = scanner.getString("station_key");
          return IStation.create(idString, keyString, worldName, x, y, z);
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }, GET_KEY_BY_LOCATION, worldName, x, y, z);
    }
  }
}
