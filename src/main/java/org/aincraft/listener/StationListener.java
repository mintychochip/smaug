package org.aincraft.listener;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Map;
import java.util.function.Predicate;
import net.kyori.adventure.key.Key;
import org.aincraft.api.event.StationActionEvent;
import org.aincraft.api.event.StationRemoveEvent;
import org.aincraft.api.event.StationRemoveEvent.RemovalCause;
import org.aincraft.container.IRecipeFetcher;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.StationHandler;
import org.aincraft.container.StationHandler.Context;
import org.aincraft.container.StationHandler.IActionContext;
import org.aincraft.container.StationHandler.IInteractionContext;
import org.aincraft.container.display.IViewModelController;
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
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
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
  private final Map<Key, StationHandler> handlers;
  private final Plugin plugin;
  private final IStationService stationService;
  private final NamespacedKey stationKey;
  private final IRecipeFetcher recipeFetcher;
  private final IViewModelController controller;

  @Inject
  public StationListener(Map<Key, StationHandler> handlers,
      Plugin plugin, IStationService stationService,
      @Named("station") NamespacedKey stationKey, IRecipeFetcher recipeFetcher,
      IViewModelController controller) {
    this.handlers = handlers;
    this.plugin = plugin;
    this.stationService = stationService;
    this.stationKey = stationKey;
    this.recipeFetcher = recipeFetcher;
    this.controller = controller;
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
    stationService.createStation(NamespacedKey.fromString(stationKey),
        blockLocation);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void onRemoveStation(final BlockBreakEvent event) {
    Block block = event.getBlock();
    Location blockLocation = block.getLocation();
    World world = blockLocation.getWorld();
    if (world == null) {
      return;
    }
    Station station = stationService.getStation(blockLocation);
    if (station == null) {
      return;
    }
    Player player = event.getPlayer();
    stationService.deleteStation(blockLocation);
    Bukkit.getPluginManager()
        .callEvent(new StationRemoveEvent(station, player, RemovalCause.PLAYER));
    for (int y = blockLocation.getBlockY() + 1; y < world.getMaxHeight(); y++) {
      Block blockAbove = world.getBlockAt(
          new Location(world, blockLocation.getBlockX(), y, blockLocation.getBlockZ()));
      if (!IS_FALLING_BLOCK_TYPE.test(blockAbove)) {
        break;
      }
      stationService.deleteStation(blockLocation);
      Bukkit.getPluginManager()
          .callEvent(new StationRemoveEvent(station, player, RemovalCause.PLAYER));
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  private void handleAction(final StationActionEvent event) {
    if (event.isCancelled()) {
      return;
    }
    Station station = event.getStation();
    StationHandler handler = handlers.get(station.getStationKey());
    if (handler == null) {
      return;
    }
    handler.handleAction(new ActionContextImpl(station, event.getPlayer(), event.getItem(),
        event.getContext().getAction(), event.getRecipe()));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleInteract(final PlayerInteractEvent event) {
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
    StationHandler handler = handlers.get(station.getStationKey());
    if (handler == null) {
      return;
    }
    EquipmentSlot hand = event.getHand();
    if(hand == EquipmentSlot.OFF_HAND) {
      return;
    }
    InteractionContextImpl interactionContext = new InteractionContextImpl(station, event);
    handler.handleInteraction(interactionContext, recipe -> {
      Bukkit.getPluginManager()
          .callEvent(new StationActionEvent(recipe, interactionContext));
    });
  }



  static final class ActionContextImpl extends ContextImpl implements IActionContext {

    private final SmaugRecipe recipe;

    ActionContextImpl(Station station, Player player, ItemStack stack, Action action,
        SmaugRecipe recipe) {
      super(station, player, stack, action);
      this.recipe = recipe;
    }

    @Override
    public SmaugRecipe getRecipe() {
      return recipe;
    }
  }

  static final class InteractionContextImpl extends ContextImpl implements IInteractionContext {

    private final PlayerInteractEvent event;

    InteractionContextImpl(Station station, PlayerInteractEvent event) {
      super(station, event.getPlayer(), event.getItem(), event.getAction());
      this.event = event;
    }

    @Override
    public void cancel() {
      event.setCancelled(true);
    }
  }

  static abstract class ContextImpl implements Context {

    private final Station station;
    private final Player player;
    @Nullable
    private final ItemStack stack;
    private final Action action;

    ContextImpl(Station station, Player player, @Nullable ItemStack stack, Action action) {
      this.station = station;
      this.player = player;
      this.stack = stack;
      this.action = action;
    }

    @NotNull
    @Override
    public Action getAction() {
      return action;
    }

    @NotNull
    @Override
    public Station getStation() {
      return station;
    }

    @Override
    public @Nullable ItemStack getItem() {
      return stack;
    }

    @NotNull
    @Override
    public Player getPlayer() {
      return player;
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
//

  //TODO: Add Explosion/Piston handling


}
