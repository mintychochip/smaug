package org.aincraft.inject.implementation.view;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.aincraft.util.Mt;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

final class DisplayStrategySelector {

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
          .add(new Vector((secondLocation.getX() - firstLocation.getX()) / 2, scale, secondLocation.getZ() - firstLocation.getZ())));
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