package org.aincraft.container.refining;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.aincraft.container.ingredient.Ingredient;
import org.aincraft.container.ingredient.IngredientList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class InventoryTransaction {
  public InventoryPlanResult planStorage(
      org.bukkit.inventory.PlayerInventory inventory,
      List<Ingredient> ingredients,
      ItemStack output) {
    return plan(inventory == null ? null : new StorageInventoryView(inventory),
        ingredients, output);
  }

  public InventoryTransactionResult applyStorage(
      org.bukkit.inventory.PlayerInventory inventory,
      InventoryPlan plan) {
    return apply(inventory == null ? null : new StorageInventoryView(inventory), plan);
  }

  public InventoryPlanResult plan(
      Inventory inventory,
      List<Ingredient> ingredients,
      ItemStack output) {
    if (inventory == null || ingredients == null || !validOutput(output)
        || !validIngredients(ingredients)) {
      return invalidPlan();
    }

    List<ItemStack> expectedContents = snapshot(inventory);
    List<ItemStack> simulatedContents = copyContents(expectedContents);
    Map<Integer, Integer> removals = new LinkedHashMap<>();
    List<Ingredient> missing = new ArrayList<>();

    for (Ingredient ingredient : ingredients) {
      int remaining = ingredient.getRequired().intValue();
      for (int slot = 0; slot < simulatedContents.size() && remaining > 0; slot++) {
        ItemStack stack = simulatedContents.get(slot);
        if (!ingredient.matches(stack)) {
          continue;
        }
        int removed = Math.min(remaining, stack.getAmount());
        remaining -= removed;
        removals.merge(slot, removed, Integer::sum);
        int remainingInStack = stack.getAmount() - removed;
        simulatedContents.set(slot, remainingInStack == 0 ? null : withAmount(stack, remainingInStack));
      }
      if (remaining > 0) {
        missing.add(ingredient.copy(remaining));
      }
    }

    if (!missing.isEmpty()) {
      return new InventoryPlanResult(InventoryPlanResult.Status.MISSING_INPUT,
          java.util.Optional.empty(), new IngredientList(missing));
    }

    List<ItemStack> afterRemoval = copyContents(simulatedContents);
    Map<Integer, ItemStack> outputChanges = new LinkedHashMap<>();
    int remainingOutput = insertOutput(inventory, simulatedContents, output);
    if (remainingOutput > 0) {
      return new InventoryPlanResult(InventoryPlanResult.Status.OUTPUT_FULL,
          java.util.Optional.empty(), IngredientList.empty());
    }

    for (int slot = 0; slot < simulatedContents.size(); slot++) {
      if (!sameStack(afterRemoval.get(slot), simulatedContents.get(slot))) {
        outputChanges.put(slot, copyStack(simulatedContents.get(slot)));
      }
    }

    InventoryPlan plan = new InventoryPlan(expectedContents, removals, outputChanges,
        simulatedContents, output);
    return new InventoryPlanResult(InventoryPlanResult.Status.SUCCESS,
        java.util.Optional.of(plan), IngredientList.empty());
  }

  public InventoryTransactionResult apply(Inventory inventory, InventoryPlan plan) {
    if (inventory == null || plan == null) {
      return new InventoryTransactionResult(InventoryTransactionResult.Status.STALE_INVENTORY);
    }

    List<ItemStack> currentContents = snapshot(inventory);
    List<ItemStack> expectedContents = plan.expectedContents();
    if (currentContents.size() != expectedContents.size()
        || !contentsEqual(currentContents, expectedContents)) {
      return new InventoryTransactionResult(InventoryTransactionResult.Status.STALE_INVENTORY);
    }
    List<ItemStack> afterRemoval = afterRemovalContents(expectedContents, plan.removals());
    if (afterRemoval == null) {
      return new InventoryTransactionResult(InventoryTransactionResult.Status.STALE_INVENTORY);
    }

    for (Map.Entry<Integer, Integer> removal : plan.removals().entrySet()) {
      int slot = removal.getKey();
      ItemStack stack = inventory.getItem(slot);
      int amount = removal.getValue();
      if (stack == null || stack.getType().isAir() || stack.getAmount() < amount) {
        return new InventoryTransactionResult(InventoryTransactionResult.Status.STALE_INVENTORY);
      }
      int remaining = stack.getAmount() - amount;
      inventory.setItem(slot, remaining == 0 ? null : withAmount(stack, remaining));
    }

    Map<Integer, ItemStack> leftovers = inventory.addItem(plan.output());
    if (leftovers == null || !leftovers.isEmpty()) {
      restoreInsertionEffects(inventory, afterRemoval);
      rollbackPlannedChanges(inventory, plan);
      return new InventoryTransactionResult(
          InventoryTransactionResult.Status.OUTPUT_INSERTION_FAILED);
    }

    restoreInsertionEffects(inventory, afterRemoval);
    for (Map.Entry<Integer, ItemStack> outputChange : plan.outputChanges().entrySet()) {
      inventory.setItem(outputChange.getKey(), outputChange.getValue());
    }
    if (!contentsEqual(snapshot(inventory), plan.finalContents())) {
      restoreInsertionEffects(inventory, afterRemoval);
      rollbackPlannedChanges(inventory, plan);
      return new InventoryTransactionResult(
          InventoryTransactionResult.Status.OUTPUT_INSERTION_FAILED);
    }

    return new InventoryTransactionResult(InventoryTransactionResult.Status.SUCCESS);
  }

  private static int insertOutput(Inventory inventory, List<ItemStack> contents, ItemStack output) {
    int remaining = output.getAmount();
    for (int slot = 0; slot < contents.size() && remaining > 0; slot++) {
      ItemStack stack = contents.get(slot);
      if (stack == null || !stack.isSimilar(output)) {
        continue;
      }
      int capacity = stackCapacity(inventory, stack, output) - stack.getAmount();
      if (capacity <= 0) {
        continue;
      }
      int added = Math.min(remaining, capacity);
      contents.set(slot, withAmount(stack, stack.getAmount() + added));
      remaining -= added;
    }

    int emptyStackCapacity = Math.min(inventory.getMaxStackSize(), output.getMaxStackSize());
    for (int slot = 0; slot < contents.size() && remaining > 0; slot++) {
      if (contents.get(slot) != null) {
        continue;
      }
      int added = Math.min(remaining, emptyStackCapacity);
      contents.set(slot, withAmount(output, added));
      remaining -= added;
    }
    return remaining;
  }

  private static int stackCapacity(Inventory inventory, ItemStack stack, ItemStack output) {
    return Math.min(inventory.getMaxStackSize(),
        Math.min(stack.getMaxStackSize(), output.getMaxStackSize()));
  }

  private static void rollbackPlannedChanges(Inventory inventory, InventoryPlan plan) {
    List<ItemStack> expected = plan.expectedContents();
    java.util.Set<Integer> touchedSlots = new java.util.HashSet<>(plan.removals().keySet());
    touchedSlots.addAll(plan.outputChanges().keySet());
    for (int slot : touchedSlots) {
      inventory.setItem(slot, copyStack(expected.get(slot)));
    }
  }
  private static List<ItemStack> afterRemovalContents(List<ItemStack> expectedContents,
      Map<Integer, Integer> removals) {
    List<ItemStack> afterRemoval = copyContents(expectedContents);
    for (Map.Entry<Integer, Integer> removal : removals.entrySet()) {
      int slot = removal.getKey();
      int amount = removal.getValue();
      if (slot < 0 || slot >= afterRemoval.size() || amount <= 0) {
        return null;
      }
      ItemStack stack = afterRemoval.get(slot);
      if (stack == null || stack.getAmount() < amount) {
        return null;
      }
      int remaining = stack.getAmount() - amount;
      afterRemoval.set(slot, remaining == 0 ? null : withAmount(stack, remaining));
    }
    return afterRemoval;
  }

  private static void restoreInsertionEffects(Inventory inventory,
      List<ItemStack> afterRemovalContents) {
    List<ItemStack> currentContents = snapshot(inventory);
    for (int slot = 0; slot < currentContents.size(); slot++) {
      if (!sameStack(currentContents.get(slot), afterRemovalContents.get(slot))) {
        inventory.setItem(slot, copyStack(afterRemovalContents.get(slot)));
      }
    }
  }

  private static boolean validIngredients(List<Ingredient> ingredients) {
    for (Ingredient ingredient : ingredients) {
      if (ingredient == null || !validRequiredAmount(ingredient.getRequired())) {
        return false;
      }
    }
    return true;
  }

  private static boolean validRequiredAmount(Number required) {
    if (required == null) {
      return false;
    }
    double asDouble = required.doubleValue();
    long asLong = required.longValue();
    return Double.isFinite(asDouble) && asDouble > 0 && asDouble == asLong
        && asLong <= Integer.MAX_VALUE;
  }

  private static boolean validOutput(ItemStack output) {
    return output != null && !output.getType().isAir() && output.getAmount() > 0;
  }

  private static InventoryPlanResult invalidPlan() {
    return new InventoryPlanResult(InventoryPlanResult.Status.INVALID_INPUT,
        java.util.Optional.empty(), IngredientList.empty());
  }

  private static List<ItemStack> snapshot(Inventory inventory) {
    List<ItemStack> contents = new ArrayList<>(inventory.getSize());
    for (int slot = 0; slot < inventory.getSize(); slot++) {
      contents.add(copyStack(inventory.getItem(slot)));
    }
    return contents;
  }

  private static List<ItemStack> copyContents(List<ItemStack> contents) {
    List<ItemStack> copy = new ArrayList<>(contents.size());
    for (ItemStack stack : contents) {
      copy.add(copyStack(stack));
    }
    return copy;
  }

  private static boolean contentsEqual(List<ItemStack> left, List<ItemStack> right) {
    if (left.size() != right.size()) {
      return false;
    }
    for (int slot = 0; slot < left.size(); slot++) {
      if (!sameStack(left.get(slot), right.get(slot))) {
        return false;
      }
    }
    return true;
  }

  private static boolean sameStack(ItemStack left, ItemStack right) {
    left = normalize(left);
    right = normalize(right);
    return Objects.equals(left, right);
  }

  private static ItemStack normalize(ItemStack stack) {
    return stack == null || stack.getType().isAir() || stack.getAmount() <= 0 ? null : stack;
  }

  private static ItemStack copyStack(ItemStack stack) {
    stack = normalize(stack);
    return stack == null ? null : stack.clone();
  }

  private static ItemStack withAmount(ItemStack stack, int amount) {
    ItemStack copy = stack.clone();
    copy.setAmount(amount);
    return copy;
  }
}
