package org.aincraft;

import java.util.List;
import java.util.function.Predicate;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.display.AnvilViewModel;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Smaug {

  @Nullable
  SmaugRecipe fetch(String recipeKey);

  @NotNull
  List<SmaugRecipe> all(@NotNull Predicate<SmaugRecipe> recipePredicate);

  default List<SmaugRecipe> all(NamespacedKey stationKey) {
    return all(recipe -> recipe.getStationKey().equals(stationKey));
  }

  default List<SmaugRecipe> all() {
    return all(recipe -> true);
  }

}
