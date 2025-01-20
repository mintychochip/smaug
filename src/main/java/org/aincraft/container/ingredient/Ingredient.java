package org.aincraft.container.ingredient;

import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Ingredient {

  boolean test(List<ItemStack> stacks);

  void add( Inventory inventory);

  void remove( List<ItemStack> stacks);

  Map<Integer,ItemStack> remove( Map<Integer,ItemStack> stackMap);

  Number getCurrentAmount( @Nullable List<ItemStack> stacks);

  @NotNull
  Number getRequired();

  @NotNull
  Component component();

  Ingredient copy(Number amount);
}
