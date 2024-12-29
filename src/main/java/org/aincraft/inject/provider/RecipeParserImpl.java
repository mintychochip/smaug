package org.aincraft.inject.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Optional;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.IRegistry.IRecipeRegistry;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.ingredient.Ingredient;
import org.aincraft.container.item.KeyedItem;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
final class RecipeParserImpl implements IParser<SmaugRecipe, IRecipeRegistry> {

  private final IIngredientParser ingredientParser;
  private final IItemRegistry itemRegistry;
  private final KeyFactory keyFactory;
  @Inject
  RecipeParserImpl(
      IIngredientParser ingredientParser, IItemRegistry itemRegistry, KeyFactory keyFactory) {
    this.ingredientParser = ingredientParser;
    this.itemRegistry = itemRegistry;
    this.keyFactory = keyFactory;
  }

  @Override
  public @Nullable SmaugRecipe parse(@Nullable ConfigurationSection section,
      @NotNull IRecipeRegistry registry) {
    if(section == null || !(section.contains("output") && section.contains("ingredients") && section.contains("type"))) {
      return null;
    }
    Optional<NamespacedKey> recipeKeyOptional = keyFactory.getKeyFromString(section.getName());
    if(recipeKeyOptional.isEmpty()) {
      return null;
    }
    KeyedItem output = itemRegistry.resolve(section.getString("output"), true);
    if(output == null) {
      return null;
    }
    List<Ingredient> ingredients = ingredientParser.parse(
        section.getConfigurationSection("ingredients"));
    if(ingredients.isEmpty()) {
      return null;
    }
    String typeString = section.getString("type");
    Optional<NamespacedKey> stationKeyOptional = keyFactory.getKeyFromString(typeString);
    return stationKeyOptional.map(
        namespacedKey -> new SmaugRecipe(output, ingredients, recipeKeyOptional.get(),
            namespacedKey)).orElse(null);
  }
}
