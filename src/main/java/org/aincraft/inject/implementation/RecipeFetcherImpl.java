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
import org.aincraft.inject.IKeyFactory;
import org.aincraft.inject.IRecipeFetcher;
import org.aincraft.listener.IStationService;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
final class RecipeFetcherImpl implements IRecipeFetcher {

  private final IRecipeParser parser;
  private final PluginConfiguration pluginConfiguration;
  private final IStationService stationService;
  private final Cache<String, SmaugRecipe> recipeCache = Caffeine.newBuilder().expireAfterWrite(1,
      TimeUnit.HOURS).build();
  private final Set<String> recipeKeys;
  private final IKeyFactory keyFactory;

  @Inject
  public RecipeFetcherImpl(
      IRecipeParser parser,
      @Named("recipe") PluginConfiguration pluginConfiguration, IStationService stationService,
      IKeyFactory keyFactory) {
    this.parser = parser;
    this.pluginConfiguration = pluginConfiguration;
    this.recipeKeys = pluginConfiguration.getKeys(false);
    this.stationService = stationService;
    this.keyFactory = keyFactory;
  }

  @Override
  public @NotNull SmaugRecipe fetch(@NotNull String recipeKey) throws ForwardReferenceException {
    Preconditions.checkNotNull(recipeKey);
    SmaugRecipe recipe = recipeCache.getIfPresent(recipeKey);
    if(recipe != null) {
      return recipe;
    }
    if(!recipeKeys.contains(recipeKey)) {
      throw new ForwardReferenceException(recipeKey);
    }

    ConfigurationSection recipeSection = pluginConfiguration.getConfigurationSection(recipeKey);
    assert recipeSection != null;
    recipe = parser.parse(recipeSection);
    recipeCache.put(recipeKey, recipe);
    return recipe;
  }

  @Override
  public @NotNull List<SmaugRecipe> all(@NotNull Predicate<SmaugRecipe> recipePredicate) {
    List<SmaugRecipe> recipes = new ArrayList<>(recipeKeys.size());
    for (String key : recipeKeys) {
      try {
        SmaugRecipe recipe = this.fetch(key);
        if(recipePredicate.test(recipe)) {
          recipes.add(recipe);
        }
      } catch (ForwardReferenceException e) {
        throw new RuntimeException(e);
      }
    }
    return recipes;
  }

  @Override
  public void refresh() {
  }
}
