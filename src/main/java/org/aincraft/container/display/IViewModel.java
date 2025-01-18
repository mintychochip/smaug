package org.aincraft.container.display;

import org.jetbrains.annotations.NotNull;

public interface IViewModel<M,V> {

  void bind(@NotNull M model, @NotNull V view);

  void update(@NotNull Object modelKey, @NotNull Object ... data);

  void remove(@NotNull Object modelKey);

  void removeAll();

  boolean isBound(@NotNull Object modelKey);
}
