package org.aincraft.container.ingredient;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Ingredient {

  boolean isSubset(Player player, Inventory inventory);

  void addIngredientToPlayer(Player player);

  Number getCurrentAmount(Player player, @Nullable Inventory inventory);

  @NotNull
  Number getAmount();

  @NotNull
  Component toItemizedComponent();

  Ingredient copy(Number amount);
}
