/*
 *
 * Copyright (C) 2025 mintychochip
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.aincraft;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.function.Predicate;
import net.kyori.adventure.key.Key;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.database.model.meta.TrackableProgressMeta;
import org.aincraft.database.model.test.IMetaStation;
import org.aincraft.exception.ForwardReferenceException;
import org.aincraft.exception.UndefinedRecipeException;
import org.aincraft.inject.IRecipeFetcher;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Smaug {

  private static ISmaugPlugin smaug;

  static void setSmaug(ISmaugPlugin smaug) {
    if (smaug != null) {
      Smaug.smaug = smaug;
    }
  }

  public static @NotNull SmaugRecipe fetchRecipe(@Nullable String recipeKey)
      throws UndefinedRecipeException, ForwardReferenceException {
    return smaug.getRecipeFetcher().fetch(recipeKey);
  }

  public static IRecipeFetcher getRecipeFetcher() {
    return smaug.getRecipeFetcher();
  }

  public static List<SmaugRecipe> fetchAllRecipes(Predicate<SmaugRecipe> recipePredicate) {
    return smaug.getRecipeFetcher().all(recipePredicate);
  }

  public static List<SmaugRecipe> fetchAllRecipes(
      IMetaStation<TrackableProgressMeta> mutableStation,
      @Nullable List<ItemStack> externalStacks) {
    Preconditions.checkNotNull(mutableStation);

    Predicate<SmaugRecipe> keyMatches = r -> r.getStationKey().equals(mutableStation.getKey());
    List<ItemStack> contents = (externalStacks == null)
        ? mutableStation.getMeta().getInventory().getContents()
        : externalStacks;

    return fetchAllRecipes(keyMatches.and(r -> r.test(contents).isSuccess()));
  }

  /**
   * Returns all the recipes associated with a station's key.
   *
   * <p>
   * If the station has the key of {@code "smaug:anvil"}, then all recipes with that key
   * will be returned.
   * </p>
   *
   * @param mutableStation whose key is being checked
   * @return list of {@code SmaugRecipe}
   */
  public static List<SmaugRecipe> fetchAllRecipes(IMetaStation mutableStation) {
    return smaug.getRecipeFetcher().all(r -> r.getStationKey().equals(mutableStation.getKey()));
  }

  public static Key resolveKey(String keyString, boolean minecraft) {
    return smaug.getKeyFactory().resolveKey(keyString, minecraft);
  }

  public static Key resolveKey(String keyString) {
    return resolveKey(keyString, false);
  }

  public static Plugin getPlugin() {
    return smaug.getPlugin();
  }
}
