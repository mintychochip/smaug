package org.aincraft.listener;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Map;
import java.util.function.Predicate;
import org.aincraft.api.event.StationInteractEvent;
import org.aincraft.api.event.StationRemoveEvent.RemovalCause;
import org.aincraft.container.InteractionKey;
import org.aincraft.container.StationHandler;
import org.aincraft.database.model.Station;
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
  private final Map<InteractionKey, StationHandler> handlers;
  private final Plugin plugin;
  private final StationService stationService;
  private final NamespacedKey stationKey;

  @Inject
  public StationListener(Map<InteractionKey, StationHandler> handlers,
      Plugin plugin, StationService stationService,
      @Named("station") NamespacedKey stationKey) {
    this.handlers = handlers;
    this.plugin = plugin;
    this.stationService = stationService;
    this.stationKey = stationKey;
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
    stationService.createStation(stationKey, blockLocation, player);
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
    stationService.deleteStation(blockLocation, player, RemovalCause.PLAYER);
    for (int y = blockLocation.getBlockY() + 1; y < world.getMaxHeight(); y++) {
      Block blockAbove = world.getBlockAt(
          new Location(world, blockLocation.getBlockX(), y, blockLocation.getBlockZ()));
      if (!IS_FALLING_BLOCK_TYPE.test(blockAbove)) {
        break;
      }
      stationService.deleteStation(blockAbove.getLocation(), player, RemovalCause.PLAYER);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleStationInteract(final PlayerInteractEvent event) {
    Block block = event.getClickedBlock();
    if (block == null) {
      return;
    }
    if (block.getType().isAir()) {
      return;
    }
    Station station = stationService.getStation(block.getLocation());
    if (station == null) {
      return;
    }
    Bukkit.getPluginManager().callEvent(new StationInteractEvent(station, event));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleStationInteractEvent(final StationInteractEvent event) {
    if (event.isCancelled()) {
      return;
    }
    InteractionKey interactionKey = new InteractionKey(event.getStation().getKey(),
        event.getInteractionType());
    StationHandler handler = handlers.get(interactionKey);
    if (handler == null) {
      return;
    }
    handler.handle(event, stationService);
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
