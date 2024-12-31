package org.aincraft.container;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

public interface IRecipeFetcher {

  @Nullable SmaugRecipe fetch(String recipeKey);

  void refresh();
}
