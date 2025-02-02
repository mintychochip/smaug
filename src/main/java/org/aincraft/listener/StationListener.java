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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.kyori.adventure.key.Key;
import org.aincraft.api.event.StationRemoveEvent;
import org.aincraft.api.event.StationRemoveEvent.RemovalCause;
import org.aincraft.database.model.meta.Meta;
import org.aincraft.database.model.meta.StationInventoryHolder;
import org.aincraft.database.model.meta.TrackableProgressMeta.StationInventory;
import org.aincraft.database.model.test.IMetaStation;
import org.aincraft.database.model.test.IStation;
import org.aincraft.database.storage.IStorage;
import org.aincraft.handler.Context;
import org.aincraft.handler.IStationHandler;
import org.aincraft.listener.StationServiceLocator.StationServices;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

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
  private final Map<Key, IStationHandler> handlers;
  private final Plugin plugin;
  private final IStationDatabaseService stationService;
  private final NamespacedKey stationKey;
  private final IStorage storage;
  private final StationServiceLocator serviceLocator;

  @Inject
  public StationListener(Map<Key, IStationHandler> handlers,
      Plugin plugin, IStationDatabaseService stationService,
      @Named("station") NamespacedKey stationKey, StationServiceLocator serviceLocator,
      IStorage storage) {
    this.handlers = handlers;
    this.plugin = plugin;
    this.stationService = stationService;
    this.stationKey = stationKey;
    this.serviceLocator = serviceLocator;
    this.storage = storage;
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
    StationServices services = serviceLocator.getServices(Key.key(keyString));
    if (services == null) {
      return;
    }
    services.getDatabaseService().createStation(Key.key(keyString), blockLocation);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void removeBlocksCheckForStation(final BlockBreakEvent event) {
    final Block block = event.getBlock();
    final Location location = block.getLocation();
    final StationServices services = serviceLocator.getServices(location);
    if (services == null) {
      return;
    }
    final IStationDatabaseService databaseService = services.getDatabaseService();
    final IStation station = databaseService.getStation(location);
    if (station == null) {
      return;
    }
    World world = station.getWorld();
    Player player = event.getPlayer();
    Bukkit.getPluginManager()
        .callEvent(new StationRemoveEvent(station, player, RemovalCause.PLAYER));
    for (int y = location.getBlockY() + 1; y < world.getMaxHeight(); y++) {
      Block blockAbove = world.getBlockAt(
          new Location(world, location.getBlockX(), y, location.getBlockZ()));
      if (!IS_FALLING_BLOCK_TYPE.test(blockAbove)) {
        break;
      }
      Bukkit.getPluginManager()
          .callEvent(new StationRemoveEvent(station, player, RemovalCause.PLAYER));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleRemoveStation(final StationRemoveEvent event) {
    if (event.isCancelled()) {
      return;
    }
    final IStation station = event.getStation();
    CompletableFuture.runAsync(() -> stationService.removeStation(station));
    if (!(station instanceof IMetaStation<?> metaStation
        && metaStation.getMeta() instanceof StationInventoryHolder holder)) {
      return;
    }
    final StationInventory inventory = holder.getInventory();
    for (ItemStack content : inventory.getContents()) {
      if (content != null) {
        station.getWorld().dropItemNaturally(
            station.getCenterLocation(), content);
      }
    }
  }

//    @EventHandler(priority = EventPriority.MONITOR)
//  private void handleUpdateStation(final StationUpdateEvent<?> event) {
//    if (event.isCancelled()) {
//      return;
//    }
//    CompletableFuture.runAsync(() -> stationService.updateStation(event.getStation()));
//  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleInteract(final PlayerInteractEvent event) {
    Block block = event.getClickedBlock();
    if (block == null) {
      return;
    }
    if (block.getType().isAir()) {
      return;
    }
    StationServices services = serviceLocator.getServices(block.getLocation());
    if (services == null) {
      return;
    }
    IStationHandler handler = services.getHandler();
    IStationDatabaseService databaseService = services.getDatabaseService();
    if (handler == null) {
      return;
    }
    EquipmentSlot hand = event.getHand();
    if (hand == EquipmentSlot.OFF_HAND) {
      return;
    }
    handler.handle(Context.create(databaseService.getStation(block.getLocation()), event));
  }
}
