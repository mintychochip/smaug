package org.aincraft.container;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;
import org.aincraft.container.item.IKeyedItem;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

public interface IRegistry<T> {

  void register(T object);

  Optional<T> get(NamespacedKey key);

  Stream<T> stream();

  Iterator<T> iterator();

  interface IItemRegistry extends IRegistry<IKeyedItem> {

    @Nullable
    IKeyedItem resolve(@Nullable String key, boolean minecraft);

    @Nullable
    default IKeyedItem resolve(@Nullable String key) {
      return resolve(key, false);
    }

    @Nullable
    IKeyedItem resolve(@Nullable NamespacedKey key, boolean minecraft);

    @Nullable
    default IKeyedItem resolve(@Nullable NamespacedKey key) {
      return resolve(key, false);
    }
  }
}
