package org.aincraft.inject.implementation.view;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import java.util.function.Function;
import org.aincraft.container.display.IViewModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AbstractViewModel<M, V, K> implements IViewModel<M, V> {

  private final Map<K, IViewModelBinding> bindings = new HashMap<>();

  private final Function<V, IViewModelBinding> viewBindingFunction;
  private final Function<M, K> modelKeyFunction;

  AbstractViewModel(Function<V, IViewModelBinding> viewBindingFunction,
      Function<M, K> modelKeyFunction) {
    this.viewBindingFunction = viewBindingFunction;
    this.modelKeyFunction = modelKeyFunction;
  }

  @Override
  public void bind(@NotNull M model, @NotNull V view) {
    final K modelKey = modelKeyFunction.apply(model);
    final IViewModelBinding binding = viewBindingFunction.apply(view);
    bindings.put(modelKey, binding);
  }

  @Override
  public void remove(@NotNull M model, Consumer<IViewModelBinding> bindingConsumer) {
    final K modelKey = modelKeyFunction.apply(model);
    final IViewModelBinding binding = bindings.remove(modelKey);
    if (bindingConsumer != null && binding != null) {
      bindingConsumer.accept(binding);
    }
  }

  @Override
  public void removeAll(@Nullable Consumer<IViewModelBinding> bindingConsumer) {
    bindings.values().forEach(binding -> {
      if (bindingConsumer != null && binding != null) {
        bindingConsumer.accept(binding);
      }
    });
    bindings.clear();
  }

  @Override
  public IViewModelBinding getBinding(M model) {
    final K key = modelKeyFunction.apply(model);
    return bindings.get(key);
  }

  @Override
  public boolean isBound(@NotNull M model) {
    final K modelKey = modelKeyFunction.apply(model);
    return bindings.containsKey(modelKey);
  }

  protected void updateBinding(M model, IViewModelBinding binding) {
    final K modelKey = modelKeyFunction.apply(model);
    bindings.put(modelKey, binding);
  }
}
