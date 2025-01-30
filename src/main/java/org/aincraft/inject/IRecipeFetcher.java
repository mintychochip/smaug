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

package org.aincraft.inject;

import java.util.List;
import java.util.function.Predicate;
import net.kyori.adventure.key.Key;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.exception.ForwardReferenceException;
import org.aincraft.exception.UndefinedRecipeException;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRecipeFetcher {

  @NotNull
  SmaugRecipe fetch(@NotNull String recipeKey) throws ForwardReferenceException, UndefinedRecipeException;

  @NotNull List<SmaugRecipe> all(@NotNull Predicate<SmaugRecipe> recipePredicate);

  default List<SmaugRecipe> all(Key stationKey, List<ItemStack> contents) {
    return all(recipe -> recipe.getStationKey().equals(stationKey) && recipe.test(contents).isSuccess());
  }
  void refresh();


}
