package org.aincraft.container;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IFactory<T, D> {

  @NotNull
  T create(@Nullable D data);
}
