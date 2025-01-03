package org.aincraft.container;

import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.container.item.IKeyedItem;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SmaugRecipe implements Keyed {

  private final IKeyedItem output;
  private final int amount;
  private final IngredientList ingredientList;
  private final NamespacedKey recipeKey;
  private final NamespacedKey stationKey;
  private final @Nullable String permission;
  private final int actions;

  public SmaugRecipe(IKeyedItem output, int amount, IngredientList ingredientList,
      NamespacedKey recipeKey, NamespacedKey stationKey,
      @Nullable String permission, int actions) {
    this.output = output;
    this.amount = amount;
    this.ingredientList = ingredientList;
    this.recipeKey = recipeKey;
    this.stationKey = stationKey;
    this.permission = permission;
    this.actions = actions;
  }

  public int getActions() {
    return actions;
  }

  public @Nullable String getPermission() {
    return permission;
  }

  @Override
  public @NotNull NamespacedKey getKey() {
    return recipeKey;
  }

  public NamespacedKey getStationKey() {
    return stationKey;
  }

  public IngredientList getIngredientList() {
    return ingredientList;
  }

  public IKeyedItem getOutput() {
    return output;
  }

  public int getAmount() {
    return amount;
  }
}
