package org.aincraft.inject.provider;

import org.aincraft.container.item.IKeyedItem;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

record KeyedItemImpl(NamespacedKey key, ItemStack reference) implements IKeyedItem {

  @Override
  public @NotNull NamespacedKey getKey() {
    return key;
  }

  @Override
  public ItemStack getReference() {
    return reference;
  }

}
