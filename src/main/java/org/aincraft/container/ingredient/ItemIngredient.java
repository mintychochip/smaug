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

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.ItemIdentifier;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ItemIngredient implements Ingredient {

  private final IKeyedItem item;
  private final NamespacedKey idKey;
  private final int amount;

  ItemIngredient(IKeyedItem item,
      NamespacedKey idKey, int amount) {
    this.item = item;
    this.idKey = idKey;
    this.amount = amount;
  }

  @Override
  public @NotNull Number getRequired() {
    return amount;
  }

  public IKeyedItem getItem() {
    return item;
  }

  @Override
  public boolean test(List<ItemStack> stacks) {
    Preconditions.checkArgument(stacks != null);
    return this.getCurrentAmount(stacks).doubleValue() >= this.getRequired()
        .doubleValue();
  }

  @Override
  public void add(Inventory inventory) {

  }

  @Override
  @Contract(value = "null->fail", pure = true)
  public void remove(List<ItemStack> contents) {
    Preconditions.checkArgument(contents != null);
    int current = getRequired().intValue();
    Iterator<ItemStack> iter = contents.iterator();
    while (current > 0 && iter.hasNext()) {
      ItemStack stack = iter.next();
      if (this.stackIsEqual(stack)) {
        int amount = stack.getAmount();
        if (current >= amount) {
          stack.setAmount(0);
          current -= amount;
        } else {
          stack.setAmount(amount - current);
          current = 0;
        }
      }
    }
  }

  @Override
  @Contract(value = "null->fail", pure = true)
  public Map<Integer, ItemStack> remove(Map<Integer, ItemStack> stackMap) {
    Preconditions.checkArgument(stackMap != null);
    int current = getRequired().intValue();
    Map<Integer, ItemStack> newStackMap = new HashMap<>();

    for (Entry<Integer, ItemStack> entry : stackMap.entrySet()) {
      ItemStack stack = entry.getValue();

      if (this.stackIsEqual(stack)) {
        int amount = stack.getAmount();
        if (current >= amount) {
          current -= amount;
          stack.setAmount(0);
        } else {
          stack.setAmount(amount - current);
          current = 0;
        }
      } else {
        newStackMap.put(entry.getKey(), stack.clone());
      }
      if (stack.getAmount() > 0) {
        newStackMap.put(entry.getKey(), stack.clone());
      }
    }

    return newStackMap;
  }

  @Override
  public Number getCurrentAmount(List<ItemStack> stacks) {
    Preconditions.checkArgument(stacks != null);
    int amount = 0;
    for (ItemStack content : stacks) {
      if (content == null) {
        continue;
      }
      if (content.getType().isAir()) {
        continue;
      }
      if (this.stackIsEqual(content)) {
        amount += content.getAmount();
      }
    }
    return amount;
  }

  private boolean stackIsEqual(ItemStack stack) {
    return item.getKey().getNamespace().equals("minecraft") && stack.isSimilar(item.getReference())
        || ItemIdentifier.compare(
        item.getReference(), stack, idKey);
  }

  @Override
  public @NotNull Component component() {
    final ItemStack reference = item.getReference();
    final ItemMeta itemMeta = reference.getItemMeta();
    assert itemMeta != null;

    @SuppressWarnings("UnstableApiUsage") final Component displayName =
        itemMeta.hasDisplayName() ? itemMeta.displayName()
            : reference.getDataOrDefault(DataComponentTypes.ITEM_NAME,
                Component.text(reference.getType().toString())).color(NamedTextColor.GRAY);
    assert displayName != null;
    return MiniMessage.miniMessage()
        .deserialize("<dark_gray><a> x <b>", Placeholder.component("a", displayName),
            Placeholder.component("b",
                Component.text(amount).color(NamedTextColor.GRAY)));
  }

  @Override
  public Ingredient copy(Number amount) {
    return new ItemIngredient(item, idKey, amount.intValue());
  }

  @Override
  public String toString() {
    return item.getReference().toString();
  }

}
