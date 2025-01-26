package org.aincraft.inject;

import java.util.List;
import java.util.function.Predicate;
import org.aincraft.container.SmaugRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRecipeFetcher {

  @Nullable
  SmaugRecipe fetch(@Nullable String recipeKey);

  @NotNull List<SmaugRecipe> all(@NotNull Predicate<SmaugRecipe> recipePredicate);

  void refresh();


}
