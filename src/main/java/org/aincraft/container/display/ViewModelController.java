package org.aincraft.container.display;

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
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationInventory;
import org.aincraft.database.model.StationInventory.InventoryType;
import org.aincraft.listener.IStationService;
import org.aincraft.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class ViewModelController {

  private final Map<Key, SmaugViewModel> viewModels = new HashMap<>();
  private final IStationService stationService;
  private static int MAX_DISPLAY = 5;
  private static final int DEFAULT_WEIGHT = 1;
  private static final Map<Key, Number> ITEM_MODEL_WEIGHTS;

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

  public ViewModelController(IStationService stationService) {
    this.stationService = stationService;
  }

  public static void setMaxDisplay(int maxDisplay) {
    MAX_DISPLAY = maxDisplay;
  }

  public void register(Key key, SmaugViewModel viewModel) {
    viewModels.put(key, viewModel);
  }

  public boolean isRegistered(Key key) {
    return viewModels.containsKey(key);
  }

  public void update(UUID stationId) {
    Station station = stationService.getStation(stationId);
    StationInventory inventory = stationService.getInventory(stationId);
    update(station, inventory, viewModels.get(station.getKey()));
  }

  public SmaugViewModel get(Key key) {
    return viewModels.get(key);
  }

  private static void update(Station station, StationInventory inventory,
      SmaugViewModel viewModel) {
    UUID stationId = inventory.getStationId();
    if (!viewModel.isBound(stationId)) {
      viewModel.bind(station, new SmaugView());
    }
    List<ItemStack> stacks =
        inventory.hasItems(InventoryType.OUTPUT) ? inventory.getItems(InventoryType.OUTPUT)
            : inventory.getItems(InventoryType.INPUT);
    if (stacks.isEmpty()) {
      return;
    }
    Map<ItemStack, Number> weightedItems = createWeightedItems(stacks);
    if (weightedItems.isEmpty()) {
      return;
    }
    Set<ItemStack> items = selectWeightedItems(weightedItems);
    Bukkit.broadcastMessage(items.toString());
    viewModel.update(stationId, items);
  }

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
    Bukkit.broadcastMessage(weightedItems + "");
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
