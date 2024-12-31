package org.aincraft.inject.provider;

import org.aincraft.container.SmaugRecipe;
import org.bukkit.configuration.ConfigurationSection;

interface IRecipeParser {

  SmaugRecipe parse(ConfigurationSection configurationSection);
}
