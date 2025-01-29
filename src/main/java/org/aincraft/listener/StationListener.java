/*
 * MIT License
 *
 * Copyright (c) 2025 mintychochip
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * provided to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.aincraft.listener;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.kyori.adventure.key.Key;
import org.aincraft.api.event.StationRemoveEvent;
import org.aincraft.api.event.StationRemoveEvent.RemovalCause;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.StationHandler;
import org.aincraft.container.StationHandler.Context;
import org.aincraft.container.StationHandler.IActionContext;
import org.aincraft.container.StationHandler.IInteractionContext;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationInventory;
import org.aincraft.database.model.Station.StationMeta;
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
import org.bukkit.scheduler.BukkitRunnable;
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

  @Inject
  public StationListener(Map<Key, StationHandler> handlers,
      Plugin plugin, IStationService stationService,
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
    stationService.createStation(NamespacedKey.fromString(stationKey),
        blockLocation);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void removeBlocksCheckForStation(final BlockBreakEvent event) {
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
    Bukkit.getPluginManager()
        .callEvent(new StationRemoveEvent(station, player, RemovalCause.PLAYER));
    for (int y = blockLocation.getBlockY() + 1; y < world.getMaxHeight(); y++) {
      Block blockAbove = world.getBlockAt(
          new Location(world, blockLocation.getBlockX(), y, blockLocation.getBlockZ()));
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
    final Station station = event.getStation();
    CompletableFuture.runAsync(() -> stationService.deleteStation(station.blockLocation()));
    final World world = station.world();
    StationMeta meta = station.getMeta();
    StationInventory stationInventory = meta.getInventory();
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

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleUpdateStation(final StationUpdateEvent event) {
    if (event.isCancelled()) {
      return;
    }
    CompletableFuture.runAsync(() -> stationService.updateStation(event.getModel()));
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
    StationHandler handler = handlers.get(station.stationKey());
    if (handler == null) {
      return;
    }
    EquipmentSlot hand = event.getHand();
    if (hand == EquipmentSlot.OFF_HAND) {
      return;
    }
    InteractionContextImpl interactionContext = new InteractionContextImpl(station, event);
    handler.handleInteraction(interactionContext, recipe -> {
      handler.handleAction(new ActionContextImpl(station, event.getPlayer(), event.getItem(),
          event.getAction(), recipe));
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

  private static List<Location> getAllStationLocations(Location location) {
    List<Location> locations = new ArrayList<>();
    return null;
  }

  //TODO: Add Explosion/Piston handling


}
