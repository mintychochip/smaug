package org.aincraft.inject.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
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
class RecipeParser {

  private final IIngredientParser ingredientParser;
  private final IItemRegistry itemRegistry;
  private final IKeyFactory keyFactory;
  private final Component listMarker;

  @Inject
  RecipeParser(
      IIngredientParser ingredientParser, IItemRegistry itemRegistry, IKeyFactory keyFactory,
      @Named("list-marker") Component listMarker) {
    this.ingredientParser = ingredientParser;
    this.itemRegistry = itemRegistry;
    this.keyFactory = keyFactory;
    this.listMarker = listMarker;
  }

  public @Nullable SmaugRecipe parse(@Nullable ConfigurationSection section) {
    if (section == null || !(section.contains("output") && section.contains("ingredients")
        && section.contains("type"))) {
      return null;
    }
    NamespacedKey recipeKey = keyFactory.getKeyFromString(section.getName(),
        true);
    if (recipeKey == null) {
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
    NamespacedKey stationKey = keyFactory.getKeyFromString(typeString, false);
    String permissionString = section.getString("permission", null);
    int actions = section.getInt("actions",1);
    return new SmaugRecipe(output, amount,
        new IngredientList(ingredients, listMarker),
        recipeKey,
        stationKey, permissionString, actions);
  }
}
