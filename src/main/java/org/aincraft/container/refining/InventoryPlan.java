package org.aincraft.container.refining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;

public final class InventoryPlan {

  private final List<ItemStack> expectedContents;
  private final Map<Integer, Integer> removals;
  private final Map<Integer, ItemStack> outputChanges;
  private final List<ItemStack> finalContents;
  private final ItemStack output;

  InventoryPlan(List<ItemStack> expectedContents,
      Map<Integer, Integer> removals,
      Map<Integer, ItemStack> outputChanges,
      List<ItemStack> finalContents,
      ItemStack output) {
    this.expectedContents = copyContents(expectedContents);
    this.removals = Map.copyOf(removals);
    this.outputChanges = copyMap(outputChanges);
    this.finalContents = copyContents(finalContents);
    this.output = requireNonEmpty(output);
    if (this.expectedContents.size() != this.finalContents.size()) {
      throw new IllegalArgumentException("inventory plan content sizes differ");
    }
  }

  public List<ItemStack> expectedContents() {
    return copyContents(expectedContents);
  }

  public Map<Integer, Integer> removals() {
    return removals;
  }

  public Map<Integer, ItemStack> outputChanges() {
    return copyMap(outputChanges);
  }

  public List<ItemStack> finalContents() {
    return copyContents(finalContents);
  }

  public ItemStack output() {
    return output.clone();
  }

  private static ItemStack requireNonEmpty(ItemStack stack) {
    Objects.requireNonNull(stack, "output");
    if (stack.getType().isAir() || stack.getAmount() <= 0) {
      throw new IllegalArgumentException("output must be a non-empty item stack");
    }
    return stack.clone();
  }

  private static List<ItemStack> copyContents(List<ItemStack> contents) {
    Objects.requireNonNull(contents, "contents");
    List<ItemStack> copy = new ArrayList<>(contents.size());
    for (ItemStack stack : contents) {
      copy.add(copyStack(stack));
    }
    return Collections.unmodifiableList(copy);
  }

  private static Map<Integer, ItemStack> copyMap(Map<Integer, ItemStack> contents) {
    Objects.requireNonNull(contents, "contents");
    Map<Integer, ItemStack> copy = new LinkedHashMap<>();
    for (Map.Entry<Integer, ItemStack> entry : contents.entrySet()) {
      copy.put(Objects.requireNonNull(entry.getKey(), "slot"), copyStack(
          Objects.requireNonNull(entry.getValue(), "output change")));
    }
    return Collections.unmodifiableMap(copy);
  }

  private static ItemStack copyStack(ItemStack stack) {
    return stack == null ? null : stack.clone();
  }
}
