/*
 * MIT License
 *
 * Copyright (c) 2025 mintychochip
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * provided to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
