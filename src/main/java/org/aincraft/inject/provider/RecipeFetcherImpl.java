package org.aincraft.inject.provider;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.aincraft.config.PluginConfiguration;
import org.aincraft.container.IRecipeFetcher;
import org.aincraft.container.SmaugRecipe;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

@Singleton
final class RecipeFetcherImpl implements IRecipeFetcher {

  private final IRecipeParser recipeParser;
  private final PluginConfiguration pluginConfiguration;

  private final Cache<String, SmaugRecipe> recipeCache = Caffeine.newBuilder().expireAfterWrite(1,
      TimeUnit.HOURS).build();
  private final Set<String> recipeKeys;

  @Inject
  public RecipeFetcherImpl(IRecipeParser recipeParser,
      @Named("recipe") PluginConfiguration pluginConfiguration) {
    this.recipeParser = recipeParser;
    this.pluginConfiguration = pluginConfiguration;

    this.recipeKeys = pluginConfiguration.getKeys(false);
  }

  @Override
  public @Nullable SmaugRecipe fetch(String recipeKey) {
    return recipeCache.get(recipeKey, key -> {
      if (!recipeKeys.contains(recipeKey)) {
        return null;
      }
      ConfigurationSection recipeSection = pluginConfiguration.getConfigurationSection(recipeKey);
      assert recipeSection != null;
      return recipeParser.parse(recipeSection);
    });
  }

  @Override
  public void refresh() {
  }
}
