package org.aincraft.inject;

import org.bukkit.NamespacedKey;

public interface IKeyFactory {
  NamespacedKey getKeyFromString(String keyString, boolean minecraft);
}
