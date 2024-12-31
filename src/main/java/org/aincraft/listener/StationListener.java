package org.aincraft.listener;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.function.Predicate;
import org.aincraft.api.event.StationCreateEvent;
import org.aincraft.api.event.StationRemoveEvent.RemovalCause;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
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
  private final Plugin plugin;
  private final StationService service;
  private final NamespacedKey stationKey;
  private final PermissionService permissionService;

  @Inject
  public StationListener(Plugin plugin, StationService service,
      @Named("station") NamespacedKey stationKey, PermissionService permissionService) {
    this.plugin = plugin;
    this.service = service;
    this.stationKey = stationKey;
    this.permissionService = permissionService;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void onPlaceStation(final BlockPlaceEvent event) {
    ItemStack itemInHand = event.getItemInHand();
    ItemMeta itemMeta = itemInHand.getItemMeta();
    if (itemMeta == null) {
      return;
    }
    PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
    String stationKey = pdc.getOrDefault(this.stationKey, PersistentDataType.STRING, "");
    if (stationKey.isEmpty()) {
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
    service.createStation(stationKey, blockLocation, player);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void onRemoveStation(final BlockBreakEvent event) {
    Block block = event.getBlock();
    Location blockLocation = block.getLocation();
    World world = blockLocation.getWorld();
    if (world == null) {
      return;
    }
    Player player = event.getPlayer();
    service.deleteStation(blockLocation, player, RemovalCause.PLAYER);
    for (int y = blockLocation.getBlockY() + 1; y < world.getMaxHeight(); y++) {
      Block blockAbove = world.getBlockAt(
          new Location(world, blockLocation.getBlockX(), y, blockLocation.getBlockZ()));
      if (!IS_FALLING_BLOCK_TYPE.test(blockAbove)) {
        break;
      }
      service.deleteStation(blockAbove.getLocation(), player, RemovalCause.PLAYER);
    }
  }

//  @EventHandler
//  private void handleDropItemsOnStationRemove(final StationBeforeRemoveEvent event) {
//    Station station = event.getStation();
//    Location stationLocation = station.getLocation();
//    World world = stationLocation.getWorld();
//    if (world == null) {
//      return;
//    }
//    service.getStationInventory(station.id(), model -> {
//      if (model == null) {
//        return;
//      }
//      new BukkitRunnable() {
//        @Override
//        public void run() {
//          Inventory[] inventories = model.getInventories();
//          for (Inventory inventory : inventories) {
//            if (inventory != null) {
//              ItemStack[] contents = inventory.getContents();
//              if (contents != null) {
//                for (ItemStack content : contents) {
//                  if (content != null && content.getAmount() > 0) {
//                    world.dropItemNaturally(stationLocation, content);
//                  }
//                }
//              }
//            }
//          }
//        }
//      }.runTask(plugin);
//    });
//  }

  //TODO: Add Explosion/Piston handling
}
