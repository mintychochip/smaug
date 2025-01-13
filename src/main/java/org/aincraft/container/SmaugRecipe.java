package org.aincraft.container;

import java.util.Arrays;
import java.util.List;
import org.aincraft.container.RecipeResult.Status;
import org.aincraft.container.ingredient.Ingredient;
import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.container.item.IKeyedItem;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class SmaugRecipe {

  private final IKeyedItem output;
  private final int amount;
  private final IngredientList ingredientList;
  private final String recipeKey;
  private final NamespacedKey stationKey;
  private final @Nullable String permission;
  private final int actions;

  public SmaugRecipe(IKeyedItem output, int amount, IngredientList ingredientList,
      String recipeKey, NamespacedKey stationKey,
      @Nullable String permission, int actions) {
    this.output = output;
    this.amount = amount;
    this.ingredientList = ingredientList;
    this.recipeKey = recipeKey;
    this.stationKey = stationKey;
    this.permission = permission;
    this.actions = actions;
  }

  public ItemStack craft() {
    ItemStack reference = output.getReference();
    ItemStack stack = new ItemStack(reference);
    stack.setAmount(amount);
    return stack;
  }

  public RecipeResult test(Player player, ItemStack ... stacks) {
    return test(player, Arrays.asList(stacks));
  }
  public RecipeResult test(Player player,
      List<ItemStack> stacks) {
    if (permission != null && !player.hasPermission(permission)) {
      return new RecipeResult(Status.FAILURE, null, "permission failure");
    }
    for (Ingredient ingredient : ingredientList) {
      if (!ingredient.test(player, stacks)) {
        return new RecipeResult(Status.FAILURE,
            ingredientList.missing(player, stacks),
            "missing ingredients");
      }
    }
    return new RecipeResult(Status.SUCCESS, null, null);
  }

  public String getKey() {
    return recipeKey;
  }

  public int getActions() {
    return actions;
  }

  public @Nullable String getPermission() {
    return permission;
  }

  public NamespacedKey getStationKey() {
    return stationKey;
  }

  public IngredientList getIngredients() {
    return ingredientList;
  }

  public IKeyedItem getOutput() {
    return output;
  }

  public int getAmount() {
    return amount;
  }

  public boolean hasActions() {
    return actions > 0;
  }
}
