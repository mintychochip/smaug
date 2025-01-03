package org.aincraft.inject.plugin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.aincraft.inject.IKeyFactory;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

@Singleton
final class KeyFactoryImpl implements IKeyFactory {

  private final Plugin plugin;

  @Inject
  public KeyFactoryImpl(Plugin plugin) {
    this.plugin = plugin;
  }

  @Nullable
  public NamespacedKey getKeyFromString(String keyString, boolean minecraft) {
    if (keyString == null) {
      return null;
    }
    String[] splitKey = keyString.toLowerCase().split(":");
    if (splitKey.length == 1) {
      if (minecraft && this.isMinecraftResource(keyString)) {
        return NamespacedKey.minecraft(keyString);
      }
      return new NamespacedKey(plugin, keyString);
    }
    return NamespacedKey.fromString(keyString);
  }

  private boolean isMinecraftResource(String resource) {
    Material material = Material.getMaterial(resource.toUpperCase());
    return material != null;
  }
}
