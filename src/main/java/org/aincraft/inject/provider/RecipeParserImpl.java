package org.aincraft.inject.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.List;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.ingredient.Ingredient;
import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.container.ingredient.ItemIngredient;
import org.aincraft.container.item.IKeyedItem;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

@Singleton
final class RecipeParserImpl implements IRecipeParser {

  private final IIngredientParser ingredientParser;
  private final IItemRegistry itemRegistry;
  private final KeyFactory keyFactory;
  private final Component listMarker;

  @Inject
  RecipeParserImpl(
      IIngredientParser ingredientParser, IItemRegistry itemRegistry, KeyFactory keyFactory,
      @Named("list-marker") Component listMarker) {
    this.ingredientParser = ingredientParser;
    this.itemRegistry = itemRegistry;
    this.keyFactory = keyFactory;
    this.listMarker = listMarker;
  }

  @Override
  public @Nullable SmaugRecipe parse(@Nullable ConfigurationSection section) {
    if (section == null || !(section.contains("output") && section.contains("ingredients")
        && section.contains("type"))) {
      return null;
    }
    Optional<NamespacedKey> recipeKeyOptional = keyFactory.getKeyFromString(section.getName(),
        true);
    if (recipeKeyOptional.isEmpty()) {
      return null;
    }
    IKeyedItem output = itemRegistry.resolve(section.getString("output"), true);
    if (output == null) {
      return null;
    }
    List<Ingredient> ingredients = ingredientParser.parse(
        section.getConfigurationSection("ingredients"));
    if (ingredients.isEmpty()) {
      return null;
    }
    String typeString = section.getString("type");
    Optional<NamespacedKey> stationKeyOptional = keyFactory.getKeyFromString(typeString, false);
    String permissionString =
        section.contains("permission") ? section.getString("permission") : null;
    return stationKeyOptional.map(
        namespacedKey -> new SmaugRecipe(output, new IngredientList(ingredients, listMarker),
            recipeKeyOptional.get(),
            namespacedKey, permissionString)).orElse(null);
  }
}
