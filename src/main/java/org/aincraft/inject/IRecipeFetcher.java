package org.aincraft.inject;

import java.util.List;
import java.util.function.Predicate;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.exception.ForwardReferenceException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRecipeFetcher {

  @NotNull
  SmaugRecipe fetch(@NotNull String recipeKey) throws ForwardReferenceException;

  @NotNull List<SmaugRecipe> all(@NotNull Predicate<SmaugRecipe> recipePredicate);

  void refresh();


}
