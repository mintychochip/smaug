package org.aincraft.inject;

import org.bukkit.NamespacedKey;

public interface IKeyFactory {
  NamespacedKey resolveKey(String keyString, boolean minecraft);
}
