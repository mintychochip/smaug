/*
 * MIT License
 *
 * Copyright (c) 2025 mintychochip
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * provided to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.aincraft.inject.implementation;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.aincraft.Smaug;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.ingredient.Ingredient;
import org.aincraft.container.ingredient.IngredientFactory;
import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.exception.ForwardReferenceException;
import org.aincraft.inject.IKeyFactory;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
final class RecipeParserImpl implements IRecipeParser {

  private final IItemRegistry itemRegistry;
  private final IKeyFactory keyFactory;
  private final IngredientFactory ingredientFactory;

  @Inject
  RecipeParserImpl(
      IItemRegistry itemRegistry, IKeyFactory keyFactory,
      IngredientFactory ingredientFactory) {
    this.itemRegistry = itemRegistry;
    this.keyFactory = keyFactory;
    this.ingredientFactory = ingredientFactory;
  }

  @Override
  public @NotNull SmaugRecipe parse(@NotNull ConfigurationSection recipeSection)
      throws ForwardReferenceException {
    Preconditions.checkNotNull(recipeSection);
    if (!(recipeSection.contains("output") && recipeSection.contains("ingredients")
        && recipeSection.contains("type"))) {
      return null;
    }
    IKeyedItem output = itemRegistry.resolve(recipeSection.getString("output"), true);
    if (output == null) {
      return null;
    }
    int amount = recipeSection.getInt("amount", 1);

    List<Ingredient> ingredients = new IngredientParser(itemRegistry,
        ingredientFactory).parseIngredients(recipeSection.getConfigurationSection("ingredients"));
    if (ingredients.isEmpty()) {
      return null;
    }
    String typeString = recipeSection.getString("type");
    NamespacedKey stationKey = keyFactory.resolveKey(typeString, false);
    String permissionString = recipeSection.getString("permission", null);
    float actions = (float) recipeSection.getDouble("actions", 1);
    return new SmaugRecipe(output, amount,
        new IngredientList(ingredients),
        recipeSection.getName(),
        stationKey, permissionString, actions);
  }

  static final class IngredientParser {

    private final IItemRegistry itemRegistry;
    private final IngredientFactory ingredientFactory;

    IngredientParser(IItemRegistry itemRegistry, IngredientFactory ingredientFactory) {
      this.itemRegistry = itemRegistry;
      this.ingredientFactory = ingredientFactory;
    }

    private @NotNull Ingredient parseIngredient(@NotNull String itemKey, int amount)
        throws ForwardReferenceException {
      Preconditions.checkNotNull(itemKey);
      IKeyedItem item = itemRegistry.resolve(itemKey, true);
      if (item == null) {
        throw new ForwardReferenceException(itemKey);
      }
      return ingredientFactory.item(item, amount);
    }

    @NotNull
    private List<Ingredient> parseItems(@NotNull ConfigurationSection itemSection)
        throws ForwardReferenceException {
      Preconditions.checkNotNull(itemSection);

      List<Ingredient> ingredients = new ArrayList<>();
      for (String key : itemSection.getKeys(false)) {
        int amount = itemSection.getInt(key, -1);
        if (amount > 0) {
          final Ingredient ingredient = parseIngredient(key, amount);
          ingredients.add(ingredient);
        }
      }
      return ingredients;
    }

    @NotNull
    private List<Ingredient> parseIngredients(@NotNull ConfigurationSection ingredientSection)
        throws ForwardReferenceException {
      Preconditions.checkNotNull(ingredientSection);

      List<Ingredient> ingredients = new ArrayList<>();
      if (ingredientSection.contains("items")) {
        ConfigurationSection itemSection = ingredientSection.getConfigurationSection("items");
        assert itemSection != null;
        ingredients.addAll(parseItems(itemSection));
      }

      return ingredients;
    }
  }
}
