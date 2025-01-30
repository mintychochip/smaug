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

import com.google.inject.Inject;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.IKeyedItemFactory;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class IngredientFactory {

  private final IKeyedItemFactory keyedItemFactory;

  @Inject
  public IngredientFactory(IKeyedItemFactory keyedItemFactory) {
    this.keyedItemFactory = keyedItemFactory;
  }

  public Ingredient item(@Nullable IKeyedItem item, int amount) {
    if (item == null) {
      return null;
    }
    return new ItemIngredient(item, keyedItemFactory.getIdentifierKey(), amount);
  }

  @Nullable
  public Ingredient item(@Nullable ItemStack itemStack, @Nullable NamespacedKey key) {
    if (itemStack == null || key == null) {
      return null;
    }
    return item(keyedItemFactory.create(itemStack, key), itemStack.getAmount());
  }
}
