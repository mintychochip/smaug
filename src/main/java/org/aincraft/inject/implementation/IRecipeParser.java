package org.aincraft.inject.implementation;

import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.ingredient.Ingredient;
import org.aincraft.exception.ForwardReferenceException;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

interface IRecipeParser {

  @NotNull
  SmaugRecipe parse(@NotNull ConfigurationSection recipeSection) throws ForwardReferenceException;

}