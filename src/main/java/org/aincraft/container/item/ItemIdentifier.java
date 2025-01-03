package org.aincraft.container.item;

import com.google.gson.Gson;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ItemIdentifier(NamespacedKey key, long version) implements Keyed {

  @Override
  public @NotNull NamespacedKey getKey() {
    return key;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NamespacedKey namespacedKey) {
      return namespacedKey.equals(this.key);
    }
    if (obj instanceof Keyed keyed) {
      return keyed.getKey().equals(this.key);
    }
    return obj.equals(this);
  }

  public static boolean contains(ItemStack itemStack, NamespacedKey identifierKey, String substr) {
    ItemIdentifier identifier = getIdentifier(itemStack, identifierKey);
    if (identifier == null) {
      return false;
    }
    return identifier.getKey().getKey().contains(substr);
  }

  public static boolean compare(@NotNull ItemStack one, @NotNull ItemStack two,
      @NotNull NamespacedKey identifierKey) {
    ItemIdentifier oneIdentifier = ItemIdentifier.getIdentifier(one, identifierKey);
    ItemIdentifier twoIdentifier = ItemIdentifier.getIdentifier(two, identifierKey);
    if (oneIdentifier == null || twoIdentifier == null) {
      return false;
    }
    return oneIdentifier.equals(twoIdentifier);
  }

  public static @Nullable ItemIdentifier getIdentifier(@Nullable ItemStack itemStack,
      @NotNull NamespacedKey identifierKey) {
    if (itemStack == null) {
      return null;
    }
    if (itemStack.getType().isAir()) {
      return null;
    }
    ItemMeta itemMeta = itemStack.getItemMeta();
    assert itemMeta != null;
    PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
    if (!pdc.has(identifierKey, PersistentDataType.STRING)) {
      return null;
    }
    String identifierString = pdc.get(identifierKey, PersistentDataType.STRING);
    return new Gson().fromJson(identifierString, ItemIdentifier.class);
  }
}