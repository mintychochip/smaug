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

package org.aincraft.inject.implementation.viewmodel;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.kyori.adventure.key.Key;
import org.aincraft.container.IFactory;
import org.aincraft.container.display.AnvilItemDisplayView;
import org.aincraft.container.display.PropertyNotFoundException;
import org.aincraft.database.model.meta.TrackableProgressMeta;
import org.aincraft.database.model.meta.TrackableProgressMeta.StationInventory;
import org.aincraft.database.model.test.IMetaStation;
import org.aincraft.util.Mt;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public final class AnvilViewModel extends
    AbstractViewModel<IMetaStation<TrackableProgressMeta>, AnvilItemDisplayView, UUID> {



  private static int MAX_DISPLAY = 3;
  private static int DEFAULT_WEIGHT = 1;
  private static float ITEM_SCALE;
  private static float BLOCK_SCALE;
  private static float TOOL_SCALE;
  private static final Map<Key, Number> ITEM_MODEL_WEIGHTS;
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
    ITEM_SCALE = 0.20f;
    BLOCK_SCALE = 0.15f;
    TOOL_SCALE = 0.35f;

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

  @Override
  public IViewModelBinding remove(IMetaStation<TrackableProgressMeta> model) {
    final IViewModelBinding binding = super.remove(model);
    if(binding != null) {
      try {
        @SuppressWarnings("unchecked")
        List<Display> displays = (List<Display>) binding.getProperty(
            "displays", List.class);
        displays.forEach(Entity::remove);
      } catch (PropertyNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    return binding;
  }

  private static Collection<Material> containsWord(String... words) {
    Predicate<Material> parent = material -> false;

    for (String word : words) {
      Predicate<Material> m = material -> material.toString().contains(word);
      parent = parent.or(m);
    }

    List<Material> subset = new ArrayList<>();
    for (Material value : Material.values()) {
      if (parent.test(value)) {
        subset.add(value);
      }
    }
    return subset;
  }

  static final class AnvilDisplayBinding extends AbstractBinding {

    @ExposedProperty("displays")
    private Collection<Display> displays;

    AnvilDisplayBinding(Collection<Display> displays) {
      this.displays = displays;
    }

    public void setDisplays(Collection<Display> displays) {
      this.displays = displays;
    }

    public Collection<Display> getDisplays() {
      return displays;
    }
  }

  @Override
  public void update(@NotNull IMetaStation<TrackableProgressMeta> model) {
    AnvilDisplayBinding binding = (AnvilDisplayBinding) this.getBinding(model);
    TrackableProgressMeta meta = model.getMeta();
    StationInventory inventory = meta.getInventory();
    List<ItemStack> contents = inventory.getContents();
    if (contents.isEmpty()) {
      this.remove(model);
      return;
    }
    Map<ItemStack, Number> weightedItems = createWeightedItems(inventory.getContents());
    if (weightedItems.isEmpty()) {
      return;
    }
    Set<ItemStack> stacks = selectWeightedItems(weightedItems);
    Map<ItemStack, Float> scaledStacks = createScaledStacks(stacks);
    //TODO: split bounding box before release
    List<Display> displays = new ArrayList<>();
    scaledStacks.forEach((key, value) -> DisplayStrategySelector.select(key, ITEM_MODEL_IS_ITEM)
        .createWrappers(key,
            randomLocation(model.getBoundingBox(4 * 0.0625f, 4 * 0.0625f), model.getWorld()),
            value)
        .forEach(wrapper -> displays.add(wrapper.delegate())));
    World world = model.getWorld();
    binding.getDisplays().forEach(Entity::remove);
    binding.setDisplays(displays);
    displays.forEach(world::addEntity);
    this.updateBinding(model, binding);
  }

  @Override
  @NotNull
  Class<? extends IViewModelBinding> getBindingClass() {
    return AnvilDisplayBinding.class;
  }

  @Override
  @NotNull IFactory<AnvilItemDisplayView, IMetaStation<TrackableProgressMeta>> getViewFactory() {
    return s -> new AnvilItemDisplayView();
  }

  @Override
  @NotNull
  IViewModelBinding viewToBinding(@NotNull AnvilItemDisplayView view) {
    return new AnvilDisplayBinding(view.getDisplays());
  }

  @Override
  @NotNull UUID modelToKey(@NotNull IMetaStation<TrackableProgressMeta> model) {
    return model.getId();
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

  record DisplayWrapper(ItemDisplay delegate) {

    static DisplayWrapper create(ItemStack stack, Location location) {
      Preconditions.checkArgument(location != null);
      World world = location.getWorld();
      if (world == null) {
        return null;
      }
      ItemDisplay display = world.createEntity(location, ItemDisplay.class);
      if (stack != null) {
        display.setItemStack(new ItemStack(stack));
      }
      return new DisplayWrapper(display);
    }

    @NotNull
    static DisplayWrapper create(DisplayWrapper d) {
      Preconditions.checkArgument(d != null);
      Location location = d.getLocation();
      DisplayWrapper display = create(d.item(), location);
      assert display != null;
      display.transformation(d.transformation());
      return display;
    }

    public void scale(Vector3f v) {
      Transformation transformation = delegate.getTransformation();
      delegate.setTransformation(
          new Transformation(transformation.getTranslation(), transformation.getLeftRotation(),
              v, transformation.getRightRotation()));
    }

    public void scale(float f) {
      scale(new Vector3f(f, f, f));
    }

    public Vector3f scale() {
      return delegate.getTransformation().getScale();
    }


    public Location getLocation() {
      return delegate.getLocation();
    }


    public void teleport(Location location) {
      delegate.teleport(location);
    }


    public void setRotation(float yaw, float pitch) {
      delegate.setRotation(yaw, pitch);
    }


    public Vector3f translation() {
      return delegate.getTransformation().getTranslation();
    }


    public void translation(Vector3f v) {
      Transformation transformation = delegate.getTransformation();
      delegate.setTransformation(
          new Transformation(v, transformation.getLeftRotation(),
              transformation.getScale(), transformation.getRightRotation()));
    }


    public Transformation transformation() {
      return delegate.getTransformation();
    }


    public void transformation(Transformation transformation) {
      delegate.setTransformation(transformation);
    }


    public ItemStack item() {
      return delegate.getItemStack();
    }


    public void item(ItemStack stack) {
      delegate.setItemStack(stack);
    }
  }

  static final class DisplayStrategySelector {

    private static final DisplayStrategy SINGLE_BLOCK;
    private static final DisplayStrategy DOUBLE_BLOCK;
    private static final DisplayStrategy TRIPLE_BLOCK;
    private static final DisplayStrategy FOUR_BLOCK;
    private static final DisplayStrategy ITEM;
    private static final int MAX_ITEM_DISPLAY_STACK = 8;
    private static final float ITEM_HORIZONTAL_CONSTANT = 2.0f;
    private static final float MATH_SQRT_2 = 1.4142135623730951f;
    private static final float PIXEL_HEIGHT = 0.0625f;

    static {
      ITEM = new ItemPileStrategy();
      SINGLE_BLOCK = new SingleBlock();
      DOUBLE_BLOCK = new DoubleBlock((SingleBlock) SINGLE_BLOCK);
      TRIPLE_BLOCK = new TripleBlock((DoubleBlock) DOUBLE_BLOCK);
      FOUR_BLOCK = new FourBlock((DoubleBlock) DOUBLE_BLOCK);
    }

    static DisplayStrategy select(ItemStack stack, Predicate<ItemStack> itemPredicate) {
      int amount = stack.getAmount();
      if (!itemPredicate.test(stack)) {
        if (amount <= 16) {
          return SINGLE_BLOCK;
        }
        if (amount <= 32) {
          return DOUBLE_BLOCK;
        }
        if (amount <= 48) {
          return TRIPLE_BLOCK;
        }
        return FOUR_BLOCK;
      }
      return ITEM;
    }

    interface DisplayStrategy {

      List<DisplayWrapper> createWrappers(ItemStack stack, Location location, float scale);
    }

    private static final class ItemPileStrategy implements DisplayStrategy {

      private ItemPileStrategy() {
      }

      @Override
      public List<DisplayWrapper> createWrappers(ItemStack stack, Location location, float scale) {
        int amount = stack.getAmount();
        int maxStackSize = stack.getMaxStackSize();
        int levels = amount * MAX_ITEM_DISPLAY_STACK / maxStackSize;
        Location origin = location.clone().add(0, PIXEL_HEIGHT * scale * 0.5, 0);

        List<DisplayWrapper> displays = new ArrayList<>();
        for (int i = 0; i < levels; i++) {
          DisplayWrapper wrapper = DisplayWrapper.create(stack,
              origin.clone().add(offsetHorizontal(scale), 0, offsetHorizontal(scale)));
          assert wrapper != null;
          wrapper.setRotation(Mt.random(180, -180), 180);
          wrapper.scale(scale);
          displays.add(wrapper);
          origin = origin.clone().add(0, PIXEL_HEIGHT * scale, 0);
        }
        return displays;
      }

      private static float offsetHorizontal(float scale) {
        double rand = Math.random() * 2 - 1;
        return (float) (PIXEL_HEIGHT * rand * ITEM_HORIZONTAL_CONSTANT * scale);
      }
    }

    private record FourBlock(DoubleBlock strategy) implements DisplayStrategy {

      @Override
      public List<DisplayWrapper> createWrappers(ItemStack stack, Location location, float scale) {
        List<DisplayWrapper> displays = strategy.createWrappers(stack, location, scale);
        DisplayWrapper d1 = displays.getFirst();
        DisplayWrapper d2 = displays.get(1);
        Location l1 = d1.getLocation();
        Location l2 = d2.getLocation();
        Vector v2 = new Vector(l2.getX() - l1.getX(), l1.getY(),
            l2.getZ() - l1.getZ());
        double angle = Math.random() > 0.5 ? 60 : -60;
        Vector v3 = rotateAroundY(v2, angle);
        DisplayWrapper d3 = DisplayWrapper.create(d1);
        d3.teleport(l1.clone().add(v3));
        d3.setRotation(Mt.random(180, -180), 0);

        Vector v4 = new Vector(v2.getX() * 0.25 + v3.getX() * 0.5, d3.scale().y(),
            v2.getZ() * 0.25 + v3.getZ() * 0.5);
        DisplayWrapper d4 = DisplayWrapper.create(d1);
        d4.teleport(l1.clone().add(v4));
        return List.of(d1, d2, d3, d4);
      }

      private static Vector rotateAroundY(Vector v2, double angle) {
        double angleRad = Math.toRadians(angle);

        double dx = v2.getX();
        double dz = v2.getZ();

        double newDx = dx * Math.cos(angleRad) - dz * Math.sin(angleRad);
        double newDz = dx * Math.sin(angleRad) + dz * Math.cos(angleRad);
        return new Vector(newDx, 0, newDz);
      }
    }

    private record TripleBlock(DoubleBlock strategy) implements DisplayStrategy {

      @Override
      public List<DisplayWrapper> createWrappers(ItemStack stack, Location location, float scale) {
        List<DisplayWrapper> displays = strategy.createWrappers(stack, location, scale);
        DisplayWrapper first = displays.getFirst();
        Location firstLocation = first.getLocation();
        DisplayWrapper second = displays.get(1);
        Location secondLocation = second.getLocation();
        DisplayWrapper third = DisplayWrapper.create(second);
        third.teleport(firstLocation.clone()
            .add(new Vector((secondLocation.getX() - firstLocation.getX()) / 2, scale,
                secondLocation.getZ() - firstLocation.getZ())));
        third.setRotation(Mt.random(180, -180), 0);
        return List.of(first, second, third);
      }
    }

    private record DoubleBlock(SingleBlock strategy) implements DisplayStrategy {

      @Override
      public List<DisplayWrapper> createWrappers(ItemStack stack, Location location, float scale) {
        List<DisplayWrapper> displays = strategy.createWrappers(stack, location, scale);
        DisplayWrapper first = displays.getFirst();
        DisplayWrapper second = DisplayWrapper.create(first);
        float randomRadian = (float) (Mt.random(180, -180) * Math.PI / 180);
        float distance = second.scale().x() * MATH_SQRT_2;
        double x = (distance * Math.cos(randomRadian));
        double z = (distance * Math.sin(randomRadian));
        second.teleport(first.getLocation().clone().add(x, 0, z));
        second.setRotation(Mt.random(180, -180), 0);
        return List.of(first, second);
      }
    }

    private static final class SingleBlock implements DisplayStrategy {

      private SingleBlock() {
      }

      @Override
      public List<DisplayWrapper> createWrappers(ItemStack stack, Location location, float scale) {
        DisplayWrapper l1 = DisplayWrapper.create(stack,
            location.clone().add(0, 0.5 * scale, 0));
        assert l1 != null;
        l1.setRotation(Mt.random(180, -180), 0);
        l1.scale(scale);
        return List.of(l1);
      }
    }
  }
}
