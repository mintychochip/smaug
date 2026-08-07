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

package org.aincraft.inject.implementation;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import net.kyori.adventure.key.Key;
import java.util.List;
import org.aincraft.Smaug;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.ingredient.Ingredient;
import org.aincraft.container.ingredient.IngredientFactory;
import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.container.refining.EfficiencyProfile;
import org.aincraft.container.refining.RefiningMetadata;
import org.aincraft.container.refining.RefiningStationType;
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

    String profession = recipeSection.getString("profession", null);
    ConfigurationSection reagentSection = recipeSection.getConfigurationSection("reagents");
    if (profession == null && reagentSection != null) {
      return null;
    }
    IngredientList reagents = IngredientList.empty();
    if (reagentSection != null) {
      List<Ingredient> parsedReagents = new IngredientParser(itemRegistry,
          ingredientFactory).parseIngredients(reagentSection);
      if (!parsedReagents.isEmpty()) {
        reagents = new IngredientList(parsedReagents);
      }
    }

    RefiningMetadata refiningMetadata = null;
    if (profession != null) {
      RefiningStationType stationType = stationKey == null ? null
          : RefiningStationType.fromKey(Key.key(stationKey.getNamespace(), stationKey.getKey()))
              .orElse(null);
      if (stationType == null || !stationType.professionKey().equals(profession)) {
        return null;
      }
      try {
        ConfigurationSection efficiency = recipeSection.getConfigurationSection("efficiency");
        String profileKey = recipeSection.getString("efficiency-profile",
            profession + "_default");
        int minimumLevel = efficiency == null ? 0 : efficiency.getInt("minimum-level", 0);
        double chancePerLevel = efficiency == null
            ? 0 : efficiency.getDouble("chance-per-level", 0);
        int maximumBonus = efficiency == null ? 0 : efficiency.getInt("maximum-bonus", 0);
        refiningMetadata = new RefiningMetadata(profession,
            recipeSection.getInt("required-level", 0),
            recipeSection.getInt("required-station-tier", 1),
            recipeSection.getInt("profession-xp", 0),
            new EfficiencyProfile(profileKey, minimumLevel, chancePerLevel, maximumBonus));
      } catch (IllegalArgumentException exception) {
        return null;
      }
    }

    return new SmaugRecipe(output, amount,
        new IngredientList(ingredients),
        recipeSection.getName(),
        stationKey, permissionString, actions, reagents, refiningMetadata);
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
