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
