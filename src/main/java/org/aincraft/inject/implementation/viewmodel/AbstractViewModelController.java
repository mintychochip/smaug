package org.aincraft.inject.implementation.viewmodel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.kyori.adventure.key.Key;
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.display.IViewModelController;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractViewModelController<M, V> implements IViewModelController<M, V> {

  protected final Map<Key, IViewModel<M, V>> viewModels = new HashMap<>();

  @Override
  public void register(@NotNull Key stationKey, @NotNull IViewModel<M, V> viewModel) {
    viewModels.put(stationKey, viewModel);
  }

  @Override
  public boolean isRegistered(@NotNull Key stationKey) {
    return viewModels.containsKey(stationKey);
  }

  @Override
  public IViewModel<M, V> get(@NotNull Key stationKey) {
    return viewModels.get(stationKey);
  }

  @Override
  public Collection<IViewModel<M, V>> getAll() {
    return viewModels.values();
  }

  @NotNull
  @Override
  public Iterator<IViewModel<M, V>> iterator() {
    return viewModels.values().iterator();
  }
}
