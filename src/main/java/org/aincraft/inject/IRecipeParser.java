package org.aincraft.inject;

import org.aincraft.container.SmaugRecipe;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

public interface IRecipeParser {
  @Nullable
  SmaugRecipe parse(@Nullable ConfigurationSection section);
}
