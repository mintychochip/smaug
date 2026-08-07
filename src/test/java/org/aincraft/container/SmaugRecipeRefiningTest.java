package org.aincraft.container;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.aincraft.container.ingredient.Ingredient;
import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.refining.EfficiencyProfile;
import org.aincraft.container.refining.RefiningMetadata;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.Test;

class SmaugRecipeRefiningTest {

  @Test
  void legacyConstructorHasNoReagentsAndIsNotRefining() {
    SmaugRecipe recipe = new SmaugRecipe(item(), 1,
        new IngredientList(List.of(ingredient("ore", 1, true))),
        "iron_ingot", Key.key("smaug:anvil"), null, 1);

    assertTrue(recipe.getReagents().isEmpty());
    assertTrue(recipe.allIngredients().asList().size() == 1);
    assertTrue(recipe.getRefiningMetadata().isEmpty());
    assertFalse(recipe.isRefining());
  }

  @Test
  void ingredientListHelpersScaleAndCombineWithoutMutatingInputs() {
    Ingredient ore = ingredient("ore", 2, true);
    Ingredient flux = ingredient("flux", 1, true);
    IngredientList primary = new IngredientList(List.of(ore));
    IngredientList reagents = new IngredientList(List.of(flux));

    IngredientList scaled = primary.scaled(3);
    IngredientList combined = primary.combinedWith(reagents);

    assertEquals(2, ore.getRequired());
    assertEquals(6, scaled.asList().get(0).getRequired());
    assertEquals(List.of(ore), primary.asList());
    assertEquals(List.of(ore, flux), combined.asList());
  }

  @Test
  void refiningRecipeExposesMetadataAndTestsReagents() {
    IngredientList primary = new IngredientList(List.of(ingredient("ore", 1, true)));
    IngredientList reagents = new IngredientList(List.of(ingredient("flux", 1, false)));
    RefiningMetadata metadata = new RefiningMetadata("smelting", 10, 2, 4,
        new EfficiencyProfile("smelting_basic", 25, 0.02, 1));
    SmaugRecipe recipe = new SmaugRecipe(item(), 1, primary, "iron_ingot",
        Key.key("smaug:smelter"), null, 1, reagents, metadata);

    assertTrue(recipe.isRefining());
    assertEquals(metadata, recipe.getRefiningMetadata().orElseThrow());
    assertEquals(2, recipe.allIngredients().asList().size());
    assertEquals(Result.Status.FAILURE, recipe.test(List.of()).getStatus());
  }

  @Test
  void rejectsInvalidEfficiencyAndRefiningRequirements() {
    assertThrows(IllegalArgumentException.class,
        () -> new EfficiencyProfile("", 0, 0, 0));
    assertThrows(IllegalArgumentException.class,
        () -> new EfficiencyProfile("bad", -1, 0, 0));
    assertThrows(IllegalArgumentException.class,
        () -> new EfficiencyProfile("bad", 0, 1.1, 0));
    assertThrows(IllegalArgumentException.class,
        () -> new EfficiencyProfile("bad", 0, 0, -1));
    assertThrows(IllegalArgumentException.class,
        () -> new RefiningMetadata("smelting", -1, 1, 0,
            new EfficiencyProfile("ok", 0, 0, 0)));
    assertThrows(IllegalArgumentException.class,
        () -> new RefiningMetadata("smelting", 0, 0, 0,
            new EfficiencyProfile("ok", 0, 0, 0)));
  }

  private static IKeyedItem item() {
    return new IKeyedItem() {
      private final NamespacedKey key = new NamespacedKey("smaug", "iron_ingot");
      private final ItemStack reference = null;

      @Override
      public NamespacedKey getKey() {
        return key;
      }

      @Override
      public ItemStack getReference() {
        return reference;
      }
    };
  }

  private static Ingredient ingredient(String name, int amount, boolean available) {
    return new Ingredient() {
      @Override
      public boolean matches(ItemStack stack) {
        return available && stack != null;
      }
      @Override
      public boolean test(List<ItemStack> stacks) {
        return available;
      }

      @Override
      public void add(Inventory inventory) {
      }

      @Override
      public void remove(List<ItemStack> stacks) {
      }

      @Override
      public Map<Integer, ItemStack> remove(Map<Integer, ItemStack> stackMap) {
        return stackMap;
      }

      @Override
      public Number getCurrentAmount(List<ItemStack> stacks) {
        return available ? amount : 0;
      }

      @Override
      public Number getRequired() {
        return amount;
      }

      @Override
      public Component component() {
        return Component.text(name);
      }

      @Override
      public Ingredient copy(Number amount) {
        return ingredient(name, amount.intValue(), available);
      }

      @Override
      public String toString() {
        return name + " x" + amount;
      }
    };
  }
}
