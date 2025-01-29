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

package org.aincraft.inject.implementation;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.IKeyedItemFactory;
import org.aincraft.container.item.ItemIdentifier;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
final class KeyedItemFactoryImpl implements IKeyedItemFactory {

  private final NamespacedKey identifierKey;

  private static final Gson gson = new Gson();

  @Inject
  KeyedItemFactoryImpl(@Named("id") NamespacedKey identifierKey) {
    this.identifierKey = identifierKey;
  }

  @Override
  public NamespacedKey getIdentifierKey() {
    return identifierKey;
  }

  @Override
  @Contract("null,null -> null")
  public @Nullable IKeyedItem create(ItemStack itemStack, NamespacedKey key) {
    return create(itemStack,new ItemIdentifier(key,1));
  }

  @Override
  @Contract(value = "null,null->fail", pure = true)
  public @Nullable IKeyedItem create(ItemStack stack, ItemIdentifier identifier) {
    Preconditions.checkArgument(stack != null);
    Preconditions.checkArgument(identifier != null);
    if (stack.getType().isAir()) {
      return null;
    }
    stack.setAmount(1);
    if(identifier.getKey().getNamespace().equals("minecraft")) {
      return new KeyedItemImpl(stack,identifier.getKey());
    }
    ItemMeta meta = stack.getItemMeta();
    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(identifierKey,PersistentDataType.STRING,gson.toJson(identifier));
    stack.setItemMeta(meta);
    return new KeyedItemImpl(stack,identifier.getKey());
  }

  record KeyedItemImpl(ItemStack reference, NamespacedKey key) implements IKeyedItem {

    @Override
    public @NotNull NamespacedKey getKey() {
      return key;
    }

    @Override
    public ItemStack getReference() {
      return reference;
    }
  }
}
