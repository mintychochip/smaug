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

package org.aincraft;

import java.util.List;
import java.util.function.Predicate;
import net.kyori.adventure.key.Key;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.database.model.Station;
import org.aincraft.exception.ForwardReferenceException;
import org.aincraft.exception.UndefinedRecipeException;
import org.aincraft.inject.IRecipeFetcher;
import org.aincraft.listener.IStationService;
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

  public static List<SmaugRecipe> fetchAllRecipes(Station station, List<ItemStack> stacks) {
    return fetchAllRecipes(
        recipe -> recipe.getStationKey().equals(station.stationKey()) && recipe.test(stacks)
            .isSuccess());
  }

  public static List<SmaugRecipe> fetchAllRecipes(Key stationKey) {
    return fetchAllRecipes(recipe -> recipe.getStationKey().equals(stationKey));
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
