package org.aincraft.container.display;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import org.aincraft.database.model.Station;
import org.aincraft.util.Mt;
import org.aincraft.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public final class AnvilViewModel implements SmaugViewModel {

  private final Map<UUID, ViewBinding> bindings = new HashMap<>();

  private static float ITEM_SCALE = 0.20f;
  private static float BLOCK_SCALE = 0.15f;
  private static float TOOL_SCALE = 0.35f;

  private static final Set<Material> ITEM_WHITELIST;
  private static final Predicate<ItemStack> ITEM_MODEL_IS_ITEM;
  private static final Predicate<ItemStack> ITEM_MODEL_IS_TOOL;

  public static void setItemScale(float itemScale) {
    ITEM_SCALE = itemScale;
  }

  public static void setBlockScale(float blockScale) {
    BLOCK_SCALE = blockScale;
  }

  public static void setToolScale(float toolScale) {
    TOOL_SCALE = toolScale;
  }

  static {
    ITEM_WHITELIST = new HashSet<>();
    ITEM_WHITELIST.addAll(Set.of(Material.HOPPER, Material.TRIPWIRE_HOOK, Material.REPEATER,
        Material.COMPARATOR, Material.LEVER, Material.CAULDRON,
        Material.BELL, Material.AMETHYST_CLUSTER, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM,
        Material.CRIMSON_FUNGUS, Material.WARPED_FUNGUS, Material.SHORT_GRASS, Material.FERN,
        Material.DEAD_BUSH, Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID,
        Material.ALLIUM, Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP,
        Material.WHITE_TULIP, Material.PINK_TULIP, Material.OXEYE_DAISY, Material.CORNFLOWER,
        Material.LILY_OF_THE_VALLEY, Material.TORCHFLOWER, Material.WITHER_ROSE,
        Material.PINK_PETALS, Material.BAMBOO, Material.SUGAR_CANE, Material.CRIMSON_ROOTS,
        Material.WARPED_ROOTS, Material.NETHER_SPROUTS, Material.WEEPING_VINES,
        Material.TWISTING_VINES, Material.VINE, Material.TALL_GRASS, Material.LARGE_FERN,
        Material.SUNFLOWER, Material.LILAC, Material.ROSE_BUSH, Material.PEONY,
        Material.PITCHER_PLANT, Material.SEAGRASS, Material.SEA_PICKLE, Material.KELP,
        Material.GLOW_LICHEN, Material.HANGING_ROOTS, Material.FROGSPAWN,
        Material.NETHER_WART, Material.LILY_PAD, Material.SCULK_VEIN,
        Material.COBWEB, Material.TURTLE_EGG, Material.CHAIN, Material.LADDER,
        Material.FLOWER_POT));
    ITEM_WHITELIST.addAll(
        containsWord("RAIL", "CANDLE", "AMETHYST_BUD", "_TORCH", "LANTERN", "CAMPFIRE", "SIGN"));

    for (Material m : Material.values()) {
      String s = m.toString();
      if (s.contains("CORAL") && !s.contains("BLOCK")) {
        ITEM_WHITELIST.add(m);
      }
    }

    ITEM_MODEL_IS_ITEM = stack -> {
      ItemMeta meta = stack.getItemMeta();
      NamespacedKey itemModel = meta.getItemModel();
      Material material = (itemModel != null && !itemModel.getNamespace().equals("minecraft"))
          ? Registry.MATERIAL.get(itemModel) : stack.getType();
      return itemModel != null || ITEM_WHITELIST.contains(material) || !material.isBlock();
    };

    ITEM_MODEL_IS_TOOL = stack -> {
      if (!ITEM_MODEL_IS_ITEM.test(stack)) {
        return false;
      }
      return false;
    };
  }

  private static Collection<Material> containsWord(String... words) {
    Predicate<Material> parent = material -> false;

    for (String word : words) {
      Predicate<Material> m = material -> material.toString().contains(word);
      parent = parent.or(m);
    }

    return Util.filterSet(Arrays.stream(Material.values()).toList(),parent);
  }

  static final class ViewBinding {

    private final Station station;
    private SmaugView view;

    ViewBinding(Station station, SmaugView view) {
      this.station = station;
      this.view = view;
    }

    public void setView(SmaugView view) {
      this.view = view;
    }

    public Station getStation() {
      return station;
    }

    public SmaugView getView() {
      return view;
    }
  }

  @Override
  public void bind(@NotNull Station station, @NotNull SmaugView view) {
    Preconditions.checkArgument(station != null);
    Preconditions.checkArgument(view != null);
    bindings.put(station.getId(), new ViewBinding(station, view));
  }

  @Override
  public void update(@NotNull UUID stationId, @NotNull Collection<ItemStack> stacks) {
    Preconditions.checkArgument(stationId != null);
    Preconditions.checkArgument(stacks != null);
    if (!bindings.containsKey(stationId)) {
      return;
    }
    ViewBinding binding = bindings.get(stationId);
    Station station = binding.getStation();
    SmaugView view = binding.getView();

    Map<ItemStack, Float> scaledStacks = createScaledStacks(stacks);
    Bukkit.broadcastMessage(scaledStacks.toString());
    //TODO: split bounding box before release
    List<Display> displays = new ArrayList<>();
    scaledStacks.forEach((key, value) -> DisplayStrategySelector.select(key, ITEM_MODEL_IS_ITEM)
        .show(key, randomLocation(station.getBoundingBox(4 * 0.0625f, 4 * 0.0625f), station.getWorld()), value)
        .forEach(wrapper -> displays.add(wrapper.delegate())));
    World world = station.getWorld();
    view.forEach(Entity::remove);
    view.setDisplays(displays);
    view.forEach(world::addEntity);
    binding.setView(view);
    bindings.put(stationId, binding);
  }

  @Override
  public void remove(@NotNull UUID stationId) {
    Preconditions.checkArgument(stationId != null);
    if (!bindings.containsKey(stationId)) {
      return;
    }
    ViewBinding binding = bindings.get(stationId);
    binding.getView().forEach(Entity::remove);
    bindings.remove(stationId);
  }

  @Override
  public boolean isBound(@NotNull UUID stationId) {
    return bindings.containsKey(stationId);
  }

  private static Location randomLocation(BoundingBox box, World world) {
    Vector max = box.getMax(), min = box.getMin();
    double x = Mt.random(max.getX(), min.getX());
    double y = Mt.random(max.getY(), min.getY());
    double z = Mt.random(max.getZ(), min.getZ());
    return new Location(world, x, y, z);
  }

  private static Map<ItemStack, Float> createScaledStacks(Collection<ItemStack> stacks) {
    Map<ItemStack, Float> scaledStacks = new HashMap<>();
    for (ItemStack stack : stacks) {
      float scale = ITEM_MODEL_IS_ITEM.test(stack)
          ? (ITEM_MODEL_IS_TOOL.test(stack) ? TOOL_SCALE : ITEM_SCALE)
          : BLOCK_SCALE;
      scaledStacks.put(stack, scale);
    }
    return scaledStacks;
  }
}
