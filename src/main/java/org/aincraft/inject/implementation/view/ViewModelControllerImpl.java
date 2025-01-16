package org.aincraft.inject.implementation.view;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.kyori.adventure.key.Key;
import org.aincraft.api.event.StationInventoryEvent;
import org.aincraft.api.event.StationRemoveEvent;
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.container.display.View;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationInventory;
import org.aincraft.listener.IStationService;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

@Singleton
final class ViewModelControllerImpl implements IViewModelController {

  private final Map<Key, IViewModel> viewModels;
  private final IStationService stationService;
  private final Plugin plugin;
  private static int MAX_DISPLAY = 3;
  private static int DEFAULT_WEIGHT = 1;
  private static final Map<Key, Number> ITEM_MODEL_WEIGHTS;

  public static void setMaxDisplay(int maxDisplay) {
    MAX_DISPLAY = maxDisplay;
  }

  public static void setDefaultWeight(int defaultWeight) {
    DEFAULT_WEIGHT = defaultWeight;
  }

  static {
    ITEM_MODEL_WEIGHTS = new HashMap<>();
    ITEM_MODEL_WEIGHTS.putAll(
        applyWeight(20, Material.GOLD_INGOT, Material.GOLD_BLOCK, Material.GOLD_NUGGET,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.NETHER_GOLD_ORE));
  }

  private static Map<Key, Number> applyWeight(Number weight, Material... materials) {
    Map<Key, Number> modelWeights = new HashMap<>();
    for (Material material : materials) {
      modelWeights.put(material.getKey(), weight);
    }
    return modelWeights;
  }

  public ViewModelControllerImpl(Map<Key, IViewModel> viewModels, IStationService stationService, Plugin plugin) {
    this.viewModels = viewModels;
    this.stationService = stationService;
    this.plugin = plugin;
  }

  @Override
  public void register(@NotNull Key key, @NotNull IViewModel viewModel) {
    viewModels.put(key, viewModel);
  }

  @Override
  public boolean isRegistered(@NotNull Key key) {
    Preconditions.checkArgument(key != null);
    return viewModels.containsKey(key);
  }

  @Override
  public IViewModel get(@NotNull Key key) {
    return viewModels.get(key);
  }

  @Override
  public Collection<IViewModel> getAll() {
    return viewModels.values();
  }

  @Override
  public void update(UUID stationId) {
    Station station = stationService.getStation(stationId);
    StationInventory inventory = stationService.getInventory(stationId);
    if (station == null || inventory == null) {
      return;
    }
    List<ItemStack> stacks = inventory.getContents();
    update(station, stacks, viewModels.get(station.getStationKey()));
  }

  @NotNull
  @Override
  public Iterator<IViewModel> iterator() {
    return viewModels.values().iterator();
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleInventoryUpdate(final StationInventoryEvent event) {
    if (event.isCancelled()) {
      return;
    }
    final StationInventory inventory = event.getInventory();
    if (inventory == null) {
      return;
    }
   // this.update(inventory.getStationId());
    CompletableFuture.supplyAsync(() -> stationService.updateInventory(inventory))
        .thenAcceptAsync(b -> {
          new BukkitRunnable() {
            @Override
            public void run() {
              if (b) {
                update(inventory.getStationId());
              }
            }
          }.runTask(plugin);
        });
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleStationRemove(final StationRemoveEvent event) {
    if(event.isCancelled()) {
      return;
    }
    Station station = event.getStation();
    IViewModel viewModel = this.get(station.getStationKey());
    if (viewModel == null) {
      return;
    }
    viewModel.remove(station.getId());
  }

  private static void update(Station station, List<ItemStack> stacks,
      IViewModel viewModel) {
    if (!viewModel.isBound(station.getId())) {
      viewModel.bind(station, new View());
    }
    if (stacks.isEmpty()) {
      viewModel.remove(station.getId());
      return;
    }
    Map<ItemStack, Number> weightedItems = createWeightedItems(stacks);
    if (weightedItems.isEmpty()) {
      return;
    }
    Set<ItemStack> items = selectWeightedItems(weightedItems);
    viewModel.update(station.getId(), items);
  }

  @NotNull
  private static Map<ItemStack, Number> createWeightedItems(Collection<ItemStack> stacks) {
    Map<ItemStack, Number> weightedItems = new HashMap<>();
    for (ItemStack stack : stacks) {
      if (stack == null || stack.getType().isAir()) {
        continue;
      }

      @SuppressWarnings("UnstableApiUsage") final Key key =
          stack.hasData(DataComponentTypes.ITEM_MODEL) ? stack.getData(
              DataComponentTypes.ITEM_MODEL) : stack.getType().getKey();
      final Number weight = ITEM_MODEL_WEIGHTS.getOrDefault(key, DEFAULT_WEIGHT);
      weightedItems.put(stack, weight);
    }
    return weightedItems;
  }

  private static Set<ItemStack> selectWeightedItems(final Map<ItemStack, Number> weightedItems) {
    if (weightedItems.isEmpty()) {
      return new HashSet<>();
    }
    int size = weightedItems.size();
    if (size == MAX_DISPLAY) {
      return weightedItems.keySet();
    }
    return weightedItems
        .entrySet()
        .stream()
        .sorted(
            (one, two) -> Double.compare(two.getValue().doubleValue(), one.getValue().doubleValue()))
        .limit(3)
        .map(Entry::getKey)
        .collect(Collectors.toSet());
  }
}
