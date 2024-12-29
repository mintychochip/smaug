package org.aincraft.container.item;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class KeyedItem implements Keyed {

  private final NamespacedKey key;
  private final ItemStack reference;

  public KeyedItem(ItemStack reference, NamespacedKey key) {
    this.reference = reference;
    this.key = key;
  }
  @Override
  public @NotNull NamespacedKey getKey() {
    return key;
  }

  public ItemStack getReference() {
    return reference;
  }

}
