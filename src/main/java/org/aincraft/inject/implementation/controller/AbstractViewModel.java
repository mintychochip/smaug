package org.aincraft.inject.implementation.controller;

import java.util.HashMap;
import java.util.Map;
import org.aincraft.container.display.IViewModel;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractViewModel<M,V> implements IViewModel<M,V> {

  @Override
  public void remove(@NotNull Object modelKey) {

  }

  @Override
  public void removeAll() {

  }

  @Override
  public boolean isBound(@NotNull Object modelKey) {
    return false;
  }
}
