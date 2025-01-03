package org.aincraft.inject.provider;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.security.KeyFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import org.aincraft.config.PluginConfiguration;
import org.aincraft.container.IRecipeFetcher;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.inject.IKeyFactory;
import org.aincraft.listener.StationService;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
final class RecipeFetcherImpl implements IRecipeFetcher {

  private final RecipeParser parser;
  private final PluginConfiguration pluginConfiguration;
  private final StationService stationService;
  private final Cache<String, SmaugRecipe> recipeCache = Caffeine.newBuilder().expireAfterWrite(1,
      TimeUnit.HOURS).build();
  private final Set<String> recipeKeys;
  private final IKeyFactory keyFactory;

  @Inject
  public RecipeFetcherImpl(
      RecipeParser parser,
      @Named("recipe") PluginConfiguration pluginConfiguration, StationService stationService,
      IKeyFactory keyFactory) {
    this.parser = parser;
    this.pluginConfiguration = pluginConfiguration;
    this.recipeKeys = pluginConfiguration.getKeys(false);
    this.stationService = stationService;
    this.keyFactory = keyFactory;
  }

  @Override
  public @Nullable SmaugRecipe fetch(String recipeKey) {
    return recipeCache.get(recipeKey, key -> {
      if (!recipeKeys.contains(recipeKey)) {
        return null;
      }
      ConfigurationSection recipeSection = pluginConfiguration.getConfigurationSection(recipeKey);
      assert recipeSection != null;
      return parser.parse(recipeSection);
    });
  }

  @Override
  public @NotNull List<SmaugRecipe> all(@NotNull Predicate<SmaugRecipe> recipePredicate) {
    List<SmaugRecipe> recipes = new ArrayList<>(recipeKeys.size());
    for (String key : recipeKeys) {
      SmaugRecipe recipe = this.fetch(key);
      if (recipe != null && recipePredicate.test(recipe)) {
        recipes.add(recipe);
      }
    }
    return recipes;
  }

  @Override
  public void refresh() {
  }
//
//  @Override
//  public RecipeResult test(SmaugRecipe recipe, Player player, Inventory inventory) {
//    if (recipe.getPermission() != null && !player.hasPermission(recipe.getPermission())) {
//      return new RecipeResult(Status.PERMISSION_FAILURE, null, "permission failure");
//    }
//    for (Ingredient ingredient : recipe.getIngredientList()) {
//      if (!ingredient.test(player, inventory)) {
//        return new RecipeResult(Status.INSUFFICIENT_RESOURCES,
//            recipe.getIngredientList().findMissing(player, inventory),
//            null);
//      }
//    }
//    return new RecipeResult(Status.SUCCESS, null, null);
//  }
}
