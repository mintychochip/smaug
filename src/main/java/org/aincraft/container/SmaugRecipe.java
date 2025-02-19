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

package org.aincraft.container;

import io.papermc.paper.datacomponent.item.ItemLore;
import java.util.List;
import net.kyori.adventure.key.Key;
import org.aincraft.container.Result.Status;
import org.aincraft.container.ingredient.Ingredient;
import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.container.item.IKeyedItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SmaugRecipe {

  public static final class RecipeResult implements Result {

    private final Status status;
    private final IngredientList missing;
    private final String error;

    RecipeResult(@NotNull Status status, @Nullable IngredientList missing,
        @Nullable String error) {
      this.status = status;
      this.missing = missing;
      this.error = error;
    }

    public Status getStatus() {
      return status;
    }

    public IngredientList getMissing() {
      return missing;
    }

    public String getError() {
      return error;
    }
  }

  private final IKeyedItem output;
  private final int amount;
  private final IngredientList ingredientList;
  private final String recipeKey;
  private final Key stationKey;
  private final @Nullable String permission;
  private final float actions;

  public SmaugRecipe(IKeyedItem output, int amount, IngredientList ingredientList,
      String recipeKey, Key stationKey,
      @Nullable String permission, float actions) {
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

  public RecipeResult test(
      List<ItemStack> stacks) {
    for (Ingredient ingredient : ingredientList) {
      if (!ingredient.test(stacks)) {
        return new RecipeResult(Status.FAILURE,
            ingredientList.findMissing(stacks),
            "missing ingredients");
      }
    }
    return new RecipeResult(Status.SUCCESS, null, null);
  }

  @NotNull
  public String getKey() {
    return recipeKey;
  }

  public float getActions() {
    return actions;
  }

  public @Nullable String getPermission() {
    return permission;
  }

  public Key getStationKey() {
    return stationKey;
  }

  public IngredientList getIngredients() {
    return ingredientList;
  }

  @SuppressWarnings("UnstableApiUsage")
  public ItemLore lore() {
    return ItemLore.lore(ingredientList.components());
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
