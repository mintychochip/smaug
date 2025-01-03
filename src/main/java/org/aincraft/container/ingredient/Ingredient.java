package org.aincraft.container.ingredient;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Ingredient {

  boolean test(Player player, Inventory inventory);

  void add(Player player, Inventory inventory);

  /**
   * Returns how much of this ingredient is currently in possession of the player,
   * or an inventory depending on the type of ingredient.
   * @param player the player to be checked
   * @param inventory the inventory to be checked
   * @return amount of the ingredient
   */
  Number getCurrentAmount(Player player, @Nullable Inventory inventory);

  @NotNull
  Number getRequired();

  @NotNull
  Component asComponent();

  Ingredient copy(Number amount);
}
