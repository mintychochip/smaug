/*
 * MIT License
 *
 * Copyright (c) 2025 mintychochip
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * provided to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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
    if(recipe == null) {
      throw new UndefinedRecipeException(recipeKey);
    }
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
