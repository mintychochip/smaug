package org.aincraft.inject.implementation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.container.ingredient.Ingredient;
import org.aincraft.container.ingredient.IngredientFactory;
import org.aincraft.container.item.IKeyedItem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
final class IngredientParser {

  private final IItemRegistry itemRegistry;
  private final IngredientFactory ingredientFactory;

  @Inject
  IngredientParser(IItemRegistry itemRegistry,
      IngredientFactory ingredientFactory) {
    this.itemRegistry = itemRegistry;
    this.ingredientFactory = ingredientFactory;
  }

  @NotNull
  @Contract("null->!null")
  public List<Ingredient> parse(ConfigurationSection ingredientSection) {
    if (ingredientSection == null) {
      return new ArrayList<>();
    }
    List<Ingredient> ingredients = new ArrayList<>();
    if (ingredientSection.contains("items")) {
      ingredientSection.getConfigurationSection(
          "items");
      List<Ingredient> items = this.parseItems(ingredientSection.getConfigurationSection("items"));
      if (items != null) {
        ingredients.addAll(items);
      }
    }
    if (ingredientSection.contains("experience")) {
      ingredients.add(ingredientFactory.experience(ingredientSection.getInt("experience")));
    }
    return ingredients;
  }

  @Nullable
  @Contract("null->null")
  private List<Ingredient> parseItems(@Nullable ConfigurationSection itemSection) {
    if (itemSection == null) {
      return null;
    }
    return itemSection.getKeys(false).stream()
        .map(k -> {
          IKeyedItem keyedItem = itemRegistry.resolve(k, true);
          int amount = itemSection.getInt(k, 1);
          return keyedItem == null ? null : ingredientFactory.item(keyedItem,amount);
        }).filter(Objects::nonNull)
        .toList();
  }
}
