package org.aincraft.inject.provider;

import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.aincraft.container.IRegistry;
import org.aincraft.container.item.IKeyedItem;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

class RegistryImpl<T extends Keyed> implements IRegistry<T> {

  protected Map<NamespacedKey, T> registry = new HashMap<>();

  @Override
  public void register(T object) {
    registry.put(object.getKey(), object);
  }

  @Override
  public Stream<T> stream() {
    return registry.values().stream();
  }

  @Override
  public Iterator<T> iterator() {
    return registry.values().iterator();
  }

  @Override
  public Optional<T> get(NamespacedKey key) {
    return Optional.ofNullable(registry.get(key));
  }

  @Singleton
  static final class ItemRegistryImpl extends RegistryImpl<IKeyedItem> implements
      IItemRegistry {

    private final KeyFactory keyFactory;
    private final KeyedItemFactoryImpl keyedItemFactory;
    ItemRegistryImpl(KeyFactory keyFactory, KeyedItemFactoryImpl keyedItemFactory) {
      this.keyFactory = keyFactory;
      this.keyedItemFactory = keyedItemFactory;
    }

    @Override
    public @Nullable IKeyedItem resolve(String key, boolean minecraft) {
      Optional<NamespacedKey> keyOptional = keyFactory.getKeyFromString(key, minecraft);
      return keyOptional.map(namespacedKey -> resolve(namespacedKey, minecraft)).orElse(null);
    }

    @Override
    public IKeyedItem resolve(NamespacedKey key, boolean minecraft) {
      if (key == null) {
        return null;
      }
      Optional<IKeyedItem> keyedItem = this.get(key);
      if (keyedItem.isPresent()) {
        return keyedItem.get();
      }
      if (minecraft && key.getNamespace().equals("minecraft")) {
        Material material = Registry.MATERIAL.get(key);
        if (material != null) {
          return keyedItemFactory.create(new ItemStack(material),key);
        }
      }
      return null;
    }
  }
}

