package org.aincraft.container.item;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public record ItemIdentifier(NamespacedKey key, long version) implements Keyed {

  @Override
  public @NotNull NamespacedKey getKey() {
    return key;
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof Keyed keyed) {
      return keyed.getKey().equals(this.key);
    }
    return obj.equals(this);
  }
}