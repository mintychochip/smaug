package org.aincraft.inject.implementation.view;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import org.aincraft.container.display.View;
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationInventory;
import org.aincraft.database.model.StationInventory.InventoryType;
import org.aincraft.listener.IStationService;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Singleton
final class ViewModelControllerImpl implements IViewModelController {

  private final Map<Key, IViewModel> viewModels;
  private final IStationService stationService;
  private static int MAX_DISPLAY = 5;
  private static int DEFAULT_WEIGHT = 1;
  private static final Map<Key, Number> ITEM_MODEL_WEIGHTS;

  public static void setMaxDisplay(int maxDisplay) {
    MAX_DISPLAY = maxDisplay;
  }

  public static void setDefaultWeight(int defaultWeight) {
    DEFAULT_WEIGHT = defaultWeight;
  }

  static {
    List<Material> materialList = Arrays.stream(Material.values()).toList();
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

  @Inject
  public ViewModelControllerImpl(Map<Key, IViewModel> viewModels, IStationService stationService) {
    this.viewModels = viewModels;
    this.stationService = stationService;
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

  public void update(UUID stationId) {
    Station station = stationService.getStation(stationId);
    StationInventory inventory = stationService.getInventory(stationId);
    List<ItemStack> stacks = inventory.hasItems(InventoryType.OUTPUT)
        ? inventory.getItems(InventoryType.OUTPUT) : inventory.getItems(InventoryType.INPUT);
    update(station, stacks, viewModels.get(station.getKey()));
  }

  private static void update(Station station, List<ItemStack> stacks,
      IViewModel viewModel) {
    if (!viewModel.isBound(station.getId())) {
      viewModel.bindView(station, new View());
    }
    if (stacks.isEmpty()) {
      return;
    }
    Map<ItemStack, Number> weightedItems = createWeightedItems(stacks);
    if (weightedItems.isEmpty()) {
      return;
    }
    Set<ItemStack> items = selectWeightedItems(weightedItems);
    viewModel.updateView(station.getId(), items);
  }

  @NotNull
  private static Map<ItemStack, Number> createWeightedItems(Collection<ItemStack> stacks) {
    Map<ItemStack, Number> weightedItems = new HashMap<>();
    for (ItemStack stack : stacks) {
      if (stack.getType().isAir()) {
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
    int count = Math.min(size, MAX_DISPLAY);
    double weightedSum = 0;
    for (Number weight : weightedItems.values()) {
      weightedSum += weight.doubleValue();
    }
    Set<ItemStack> result = new HashSet<>();
    while (result.size() < count) {
      double random = Math.random() * weightedSum;
      double current = 0;

      for (Entry<ItemStack, Number> entry : weightedItems.entrySet()) {
        double weight = entry.getValue().doubleValue();
        current += weight;
        if (current >= random) {
          result.add(entry.getKey());
          if (result.size() >= count) {
            break;
          }
        }
      }
    }
    return result;
  }
}
