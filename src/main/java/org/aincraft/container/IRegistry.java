package org.aincraft.container;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.aincraft.container.item.KeyedItem;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

public interface IRegistry<T> {

  void register(T object);

  Optional<T> get(NamespacedKey key);

  Stream<T> stream();

  Iterator<T> iterator();

  interface IItemRegistry extends IRegistry<KeyedItem> {

    @Nullable
    KeyedItem resolve(@Nullable String key, boolean minecraft);

    @Nullable
    default KeyedItem resolve(@Nullable String key) {
      return resolve(key, false);
    }

    @Nullable
    KeyedItem resolve(@Nullable NamespacedKey key, boolean minecraft);

    @Nullable
    default KeyedItem resolve(@Nullable NamespacedKey key) {
      return resolve(key, false);
    }
  }

  interface IRecipeRegistry extends IRegistry<SmaugRecipe> {

    List<SmaugRecipe> findAll(NamespacedKey stationKey);
  }
}
