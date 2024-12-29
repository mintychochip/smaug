package org.aincraft.inject.provider;

import java.util.List;
import org.aincraft.container.ingredient.Ingredient;
import org.bukkit.configuration.ConfigurationSection;

interface IIngredientParser {
  List<Ingredient> parse(ConfigurationSection ingredientSection);
}
