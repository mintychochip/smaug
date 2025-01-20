package org.aincraft.inject.implementation;

import org.aincraft.container.SmaugRecipe;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

interface IRecipeParser {
  @Nullable
  SmaugRecipe parse(@Nullable ConfigurationSection section);
}
