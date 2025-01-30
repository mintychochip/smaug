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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class IngredientList implements Iterable<Ingredient> {

  private List<Ingredient> delegate;

  public List<Component> components() {
    return delegate.stream().map(i -> MiniMessage.miniMessage()
            .deserialize("<italic:false><white> * <a>", Placeholder.component("a", i.component())))
        .toList();
  }

  public Map<Integer, ItemStack> remove(Map<Integer, ItemStack> stackMap) {
    Map<Integer, ItemStack> removed = new HashMap<>(stackMap);
    for (Ingredient i : delegate) {
      removed = i.remove(removed);
    }
    return removed;
  }

  public IngredientList(List<Ingredient> ingredients) {
    this.delegate = ingredients;
  }

  public void addIngredient(Ingredient ingredient) {
    delegate.add(ingredient);
  }

  public void setIngredients(List<Ingredient> ingredients) {
    this.delegate = ingredients;
  }

  public boolean isEmpty() {
    return delegate.isEmpty();
  }
  @NotNull
  @Override
  public Iterator<Ingredient> iterator() {
    return delegate.iterator();
  }

  public Stream<Ingredient> stream() {
    return delegate.stream();
  }

  public IngredientList findMissing(
      List<ItemStack> stacks) {
    List<Ingredient> missing = new ArrayList<>();
    for (Ingredient ingredient : this.delegate) {
      if (!ingredient.test(stacks)) {
        double amount =
            ingredient.getRequired().doubleValue() - ingredient.getCurrentAmount(stacks)
                .doubleValue();
        missing.add(ingredient.copy(amount));
      }
    }
    return new IngredientList(missing);
  }

  @Override
  public String toString() {
    return delegate.toString();
  }
}
