package org.aincraft.inject.implementation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import org.aincraft.config.PluginConfiguration;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.exception.ForwardReferenceException;
import org.aincraft.exception.UndefinedRecipeException;
import org.aincraft.inject.IRecipeFetcher;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

@Singleton
final class RecipeFetcherImpl implements IRecipeFetcher {

  private final IRecipeParser recipeParser;
  private final PluginConfiguration pluginConfiguration;
  private final Cache<String, SmaugRecipe> recipeCache = Caffeine.newBuilder().expireAfterWrite(1,
      TimeUnit.HOURS).build();

  @Inject
  public RecipeFetcherImpl(
      IRecipeParser recipeParser,
      @Named("recipe") PluginConfiguration pluginConfiguration) {
    this.recipeParser = recipeParser;
    this.pluginConfiguration = pluginConfiguration;
  }

  @Override
  public @NotNull SmaugRecipe fetch(@NotNull String recipeKey)
      throws ForwardReferenceException, UndefinedRecipeException {
    Preconditions.checkNotNull(recipeKey);
    SmaugRecipe recipe = recipeCache.getIfPresent(recipeKey);
    if (recipe != null) {
      return recipe;
    }
    if (!pluginConfiguration.getKeys(false).contains(recipeKey)) {
      throw new UndefinedRecipeException(recipeKey);
    }

    final ConfigurationSection recipeSection = pluginConfiguration.getConfigurationSection(
        recipeKey);
    assert recipeSection != null;
    recipe = recipeParser.parse(recipeSection);
    recipeCache.put(recipeKey, recipe);
    return recipe;
  }

  @Override
  public @NotNull List<SmaugRecipe> all(@NotNull Predicate<SmaugRecipe> recipePredicate) {
    Set<String> recipeKeys = pluginConfiguration.getKeys(false);
    List<SmaugRecipe> recipes = new ArrayList<>(recipeKeys.size());
    for (String key : recipeKeys) {
      try {
        SmaugRecipe recipe = fetch(key);
        if (recipePredicate.test(recipe)) {
          recipes.add(recipe);
        }
      } catch (ForwardReferenceException | UndefinedRecipeException e) {
        throw new RuntimeException(e);
      }
    }
    return recipes;
  }

  @Override
  public void refresh() {
  }
}
