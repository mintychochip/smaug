package org.aincraft.container.ingredient;

import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Ingredient {

  boolean test(Player player, List<ItemStack> stacks);

  void add(Player player, Inventory inventory);

  void remove(Player player, List<ItemStack> stacks);

  Map<Integer,ItemStack> remove(Player player, Map<Integer,ItemStack> stackMap);

  Number getCurrentAmount(Player player, @Nullable List<ItemStack> stacks);

  @NotNull
  Number getRequired();

  @NotNull
  Component component();

  Ingredient copy(Number amount);
}
