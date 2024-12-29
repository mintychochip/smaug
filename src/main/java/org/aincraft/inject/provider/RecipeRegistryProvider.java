package org.aincraft.inject.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import java.util.Set;
import org.aincraft.config.PluginConfiguration;
import org.aincraft.container.IRegistry.IRecipeRegistry;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.inject.provider.RegistryImpl.RecipeRegistryImpl;

final class RecipeRegistryProvider implements Provider<IRecipeRegistry> {

  private final PluginConfiguration recipeConfiguration;
  private final IParser<SmaugRecipe, IRecipeRegistry> parser;

  @Inject
  RecipeRegistryProvider(@Named("recipe-configuration") PluginConfiguration recipeConfiguration,
      IParser<SmaugRecipe, IRecipeRegistry> parser) {
    this.recipeConfiguration = recipeConfiguration;
    this.parser = parser;
  }

  @Override
  public IRecipeRegistry get() {
    RecipeRegistryImpl registry = new RecipeRegistryImpl();
    Set<String> recipeKeys = recipeConfiguration.getKeys(false);
    for (String recipeKey : recipeKeys) {
      SmaugRecipe recipe = parser.parse(recipeConfiguration.getConfigurationSection(recipeKey),
          registry);
      if (recipe != null) {
        registry.register(recipe);
      }
    }
    return registry;
  }
}
