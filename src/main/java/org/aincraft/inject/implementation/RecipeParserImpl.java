package org.aincraft.inject.implementation;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.ingredient.Ingredient;
import org.aincraft.container.ingredient.IngredientFactory;
import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.exception.ForwardReferenceException;
import org.aincraft.inject.IKeyFactory;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
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
        ingredientFactory).parseIngredients(recipeSection);
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
        int amount = itemSection.getInt("amount", -1);
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
        ;
        ConfigurationSection itemSection = ingredientSection.getConfigurationSection("items");
        assert itemSection != null;
        ingredients.addAll(parseItems(itemSection));
      }

      return ingredients;
    }
  }
}
