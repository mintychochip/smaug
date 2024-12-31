package org.aincraft.container.item;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IKeyedItemFactory {

  @Nullable
  IKeyedItem create(ItemStack itemStack, NamespacedKey key);

  NamespacedKey getIdentifierKey();
}
