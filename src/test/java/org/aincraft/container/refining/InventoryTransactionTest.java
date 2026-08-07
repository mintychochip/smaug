package org.aincraft.container.refining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import org.aincraft.container.ingredient.Ingredient;
import org.aincraft.container.ingredient.IngredientFactory;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.IKeyedItemFactory;
import org.aincraft.container.item.ItemIdentifier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class InventoryTransactionTest {

  private static final NamespacedKey IDENTIFIER_KEY = new NamespacedKey("smaug", "id");
  private InventoryTransaction transaction;

  @BeforeEach
  void setUp() {
    MockBukkit.mock();
    transaction = new InventoryTransaction();
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }
  @Test
  void missingIngredientDoesNotMutateInventory() {
    Inventory inventory = inventory();
    inventory.setItem(0, new ItemStack(Material.RAW_IRON, 1));

    InventoryPlanResult result = transaction.plan(inventory,
        List.of(itemIngredient(Material.RAW_IRON, 2)), output());

    assertEquals(InventoryPlanResult.Status.MISSING_INPUT, result.status());
    assertEquals(1, inventory.getItem(0).getAmount());
    assertTrue(result.plan().isEmpty());
    assertEquals(1, result.missing().asList().size());
  }

  @Test
  void invalidInputReturnsFailureWithoutCreatingAPlan() {
    InventoryPlanResult result = transaction.plan(inventory(), List.of(), null);

    assertEquals(InventoryPlanResult.Status.INVALID_INPUT, result.status());
    assertTrue(result.plan().isEmpty());
    assertTrue(result.missing().isEmpty());
  }

  @Test
  void removesExactQuantitiesAcrossMatchingStacksAndInsertsOutput() {
    Inventory inventory = inventory();
    inventory.setItem(0, new ItemStack(Material.RAW_IRON, 1));
    inventory.setItem(1, new ItemStack(Material.RAW_IRON, 2));

    InventoryPlanResult planned = transaction.plan(inventory,
        List.of(itemIngredient(Material.RAW_IRON, 3)), output());
    InventoryTransactionResult applied = transaction.apply(inventory,
        planned.plan().orElseThrow());

    assertEquals(InventoryTransactionResult.Status.SUCCESS, applied.status());
    assertEquals(Material.IRON_INGOT, inventory.getItem(0).getType());
    assertEquals(1, inventory.getItem(0).getAmount());
    assertNull(inventory.getItem(1));
    assertNull(inventory.getItem(2));
  }

  @Test
  void matchesCustomItemIdentityWithoutAcceptingUnidentifiedMaterial() {
    Inventory inventory = inventory();
    inventory.setItem(0, new ItemStack(Material.PAPER, 1));
    inventory.setItem(1, customStack("flux"));

    InventoryPlanResult result = transaction.plan(inventory,
        List.of(customIngredient("flux", 1)), output());

    assertEquals(InventoryPlanResult.Status.SUCCESS, result.status());
    assertEquals(1, inventory.getItem(0).getAmount());
  }

  @Test
  void mergesOutputBeforeUsingAnEmptySlot() {
    Inventory inventory = inventory();
    inventory.setItem(0, new ItemStack(Material.RAW_IRON, 1));
    inventory.setItem(1, new ItemStack(Material.IRON_INGOT, 63));

    InventoryPlanResult planned = transaction.plan(inventory,
        List.of(itemIngredient(Material.RAW_IRON, 1)), new ItemStack(Material.IRON_INGOT, 2));
    transaction.apply(inventory, planned.plan().orElseThrow());

    assertEquals(64, inventory.getItem(1).getAmount());
    assertEquals(1, inventory.getItem(0).getAmount());
  }

  @Test
  void fullOutputLeavesInputsUntouched() {
    Inventory inventory = inventory();
    inventory.setItem(0, new ItemStack(Material.RAW_IRON, 2));
    for (int slot = 1; slot < inventory.getSize(); slot++) {
      inventory.setItem(slot, new ItemStack(Material.IRON_INGOT, 64));
    }

    InventoryPlanResult result = transaction.plan(inventory,
        List.of(itemIngredient(Material.RAW_IRON, 1)), output());

    assertEquals(InventoryPlanResult.Status.OUTPUT_FULL, result.status());
    assertEquals(2, inventory.getItem(0).getAmount());
  }

  @Test
  void stalePlanIsRejectedWithoutMutation() {
    Inventory inventory = inventory();
    inventory.setItem(0, new ItemStack(Material.RAW_IRON, 1));
    InventoryPlan plan = transaction.plan(inventory,
        List.of(itemIngredient(Material.RAW_IRON, 1)), output()).plan().orElseThrow();

    inventory.setItem(0, new ItemStack(Material.RAW_IRON, 2));
    InventoryTransactionResult result = transaction.apply(inventory, plan);

    assertEquals(InventoryTransactionResult.Status.STALE_INVENTORY, result.status());
    assertEquals(2, inventory.getItem(0).getAmount());
  }

  @Test
  void unexpectedInsertionLeftoverRestoresOnlyPlannedChanges() {
    Inventory delegate = inventory();
    delegate.setItem(0, new ItemStack(Material.RAW_IRON, 1));
    delegate.setItem(4, new ItemStack(Material.DIAMOND, 1));
    Inventory inventory = insertionRejecting(delegate);
    InventoryPlan plan = transaction.plan(inventory,
        List.of(itemIngredient(Material.RAW_IRON, 1)), output()).plan().orElseThrow();

    InventoryTransactionResult result = transaction.apply(inventory, plan);

    assertEquals(InventoryTransactionResult.Status.OUTPUT_INSERTION_FAILED, result.status());
    assertEquals(1, delegate.getItem(0).getAmount());
    assertEquals(Material.DIAMOND, delegate.getItem(4).getType());
    assertNull(delegate.getItem(1));
  }
  private static Inventory inventory() {
    return Bukkit.createInventory(null, 9);
  }

  private static ItemStack output() {
    return new ItemStack(Material.IRON_INGOT, 1);
  }

  private static Ingredient itemIngredient(Material material, int amount) {
    return ingredient(new SimpleItem(NamespacedKey.minecraft(material.name().toLowerCase()),
        new ItemStack(material)), amount);
  }

  private static Ingredient customIngredient(String key, int amount) {
    return ingredient(new SimpleItem(new NamespacedKey("smaug", key), customStack(key)), amount);
  }

  private static Ingredient ingredient(IKeyedItem item, int amount) {
    return new IngredientFactory(new TestItemFactory()).item(item, amount);
  }

  private static ItemStack customStack(String key) {
    ItemStack stack = new ItemStack(Material.PAPER);
    ItemMeta meta = stack.getItemMeta();
    ItemIdentifier identifier = new ItemIdentifier(new NamespacedKey("smaug", key), 1);
    meta.getPersistentDataContainer().set(IDENTIFIER_KEY,
        org.bukkit.persistence.PersistentDataType.STRING, new Gson().toJson(identifier));
    stack.setItemMeta(meta);
    return stack;
  }

  private static Inventory insertionRejecting(Inventory delegate) {
    return (Inventory) Proxy.newProxyInstance(Inventory.class.getClassLoader(),
        new Class<?>[]{Inventory.class}, (proxy, method, args) -> {
          if (method.getName().equals("addItem")) {
            ItemStack[] stacks = (ItemStack[]) args[0];
            return new java.util.HashMap<>(Map.of(0, stacks[0].clone()));
          }
          if (method.getDeclaringClass() == Object.class) {
            return method.invoke(delegate, args);
          }
          return method.invoke(delegate, args);
        });
  }

  private static final class TestItemFactory implements IKeyedItemFactory {
    @Override
    public IKeyedItem create(ItemStack itemStack, NamespacedKey key) {
      return new SimpleItem(key, itemStack);
    }

    @Override
    public IKeyedItem create(ItemStack stack, ItemIdentifier identifier) {
      return new SimpleItem(identifier.getKey(), stack);
    }

    @Override
    public NamespacedKey getIdentifierKey() {
      return IDENTIFIER_KEY;
    }
  }

  private record SimpleItem(NamespacedKey key, ItemStack reference) implements IKeyedItem {
    @Override
    public NamespacedKey getKey() {
      return key;
    }

    @Override
    public ItemStack getReference() {
      return reference;
    }
  }
}
