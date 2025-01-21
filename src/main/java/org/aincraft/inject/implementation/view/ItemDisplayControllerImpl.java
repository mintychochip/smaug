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
import java.util.stream.Collectors;
import net.kyori.adventure.key.Key;
import org.aincraft.api.event.StationRemoveEvent;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.display.AnvilItemDisplayView;
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationInventory;
import org.aincraft.database.model.Station.StationMeta;
import org.aincraft.listener.IStationService;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

@Singleton
final class ItemDisplayControllerImpl implements IViewModelController<Station, AnvilItemDisplayView> {

  private final Map<Key, IViewModel<Station, AnvilItemDisplayView>> viewModels;
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

  public ItemDisplayControllerImpl(Map<Key, IViewModel<Station, AnvilItemDisplayView>> viewModels,
      IStationService stationService, Plugin plugin) {
    this.viewModels = viewModels;
    this.stationService = stationService;
    this.plugin = plugin;
  }

  @Override
  public void register(@NotNull Key stationKey,
      @NotNull IViewModel<Station, AnvilItemDisplayView> viewModel) {
    viewModels.put(stationKey, viewModel);
  }

  @Override
  public boolean isRegistered(@NotNull Key stationKey) {
    Preconditions.checkArgument(stationKey != null);
    return viewModels.containsKey(stationKey);
  }

  @Override
  public IViewModel<Station, AnvilItemDisplayView> get(@NotNull Key stationKey) {
    return viewModels.get(stationKey);
  }

  private void update(Station model) {
    StationMeta meta = model.getMeta();
    StationInventory inventory = meta.getInventory();
    List<ItemStack> stacks = inventory.getContents();
    final IViewModel<Station, AnvilItemDisplayView> viewModel = viewModels.get(model.getStationKey());
    if(!viewModel.isBound(model.getId())) {
      viewModel.bind(model,new AnvilItemDisplayView());
    }
    if(stacks.isEmpty()) {
      viewModel.remove(model.getId());
      return;
    }
    Map<ItemStack, Number> weightedItems = createWeightedItems(stacks);
    if (weightedItems.isEmpty()) {
      return;
    }
    Set<ItemStack> items = selectWeightedItems(weightedItems);
    viewModel.update(model, items);
  }

  @Override
  public Collection<IViewModel<Station, AnvilItemDisplayView>> getAll() {
    return viewModels.values();
  }

  @NotNull
  @Override
  public Iterator<IViewModel<Station, AnvilItemDisplayView>> iterator() {
    return viewModels.values().iterator();
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleUpdateItemDisplay(final StationUpdateEvent event) {
    if (event.isCancelled()) {
      return;
    }
    Station model = event.getModel();
    this.update(model);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleRemoveItemDisplay(final StationRemoveEvent event) {
    if (event.isCancelled()) {
      return;
    }
    Station station = event.getStation();
    IViewModel<Station, AnvilItemDisplayView> viewModel = this.get(station.getStationKey());
    if (viewModel == null) {
      return;
    }
    viewModel.remove(station.getId());
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
            (one, two) -> Double.compare(two.getValue().doubleValue(),
                one.getValue().doubleValue()))
        .limit(3)
        .map(Entry::getKey)
        .collect(Collectors.toSet());
  }
}
