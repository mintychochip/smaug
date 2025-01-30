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
