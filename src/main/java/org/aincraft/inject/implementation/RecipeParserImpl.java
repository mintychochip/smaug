package org.aincraft.inject.implementation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.ingredient.Ingredient;
import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.inject.IKeyFactory;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

@Singleton
final class RecipeParserImpl implements IRecipeParser {

  private final IngredientParser ingredientParser;
  private final IItemRegistry itemRegistry;
  private final IKeyFactory keyFactory;

  @Inject
  RecipeParserImpl(
      IngredientParser ingredientParser, IItemRegistry itemRegistry, IKeyFactory keyFactory) {
    this.ingredientParser = ingredientParser;
    this.itemRegistry = itemRegistry;
    this.keyFactory = keyFactory;
  }

  @Override
  public @Nullable SmaugRecipe parse(@Nullable ConfigurationSection section) {
    if (section == null || !(section.contains("output") && section.contains("ingredients")
        && section.contains("type"))) {
      return null;
    }
    IKeyedItem output = itemRegistry.resolve(section.getString("output"), true);
    if (output == null) {
      return null;
    }
    int amount = section.getInt("amount", 1);
    List<Ingredient> ingredients = ingredientParser.parse(
        section.getConfigurationSection("ingredients"));
    if (ingredients.isEmpty()) {
      return null;
    }
    String typeString = section.getString("type");
    NamespacedKey stationKey = keyFactory.resolveKey(typeString, false);
    String permissionString = section.getString("permission", null);
    float actions = (float) section.getDouble("actions", 1);
    return new SmaugRecipe(output, amount,
        new IngredientList(ingredients),
        section.getName(),
        stationKey, permissionString, actions);
  }
}
