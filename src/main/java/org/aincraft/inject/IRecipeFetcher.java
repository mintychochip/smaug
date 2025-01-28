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
