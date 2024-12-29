package org.aincraft.inject.provider;

import com.google.inject.Inject;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

final class KeyFactory {

  private final Plugin plugin;
  @Inject
  public KeyFactory(Plugin plugin) {
    this.plugin = plugin;
  }

  public Optional<NamespacedKey> getKeyFromString(String keyString) {
    if (keyString == null) {
      return Optional.empty();
    }
    String[] splitKey = keyString.toLowerCase().split(":");
    if (splitKey.length == 1) {
      if (this.isMinecraftResource(keyString)) {
        return Optional.of(NamespacedKey.minecraft(keyString));
      }
      return Optional.of(new NamespacedKey(plugin, keyString));
    }
    return Optional.ofNullable(NamespacedKey.fromString(keyString));
  }

  private boolean isMinecraftResource(String resource) {
    Material material = Material.getMaterial(resource.toUpperCase());
    return material != null;
  }
}
