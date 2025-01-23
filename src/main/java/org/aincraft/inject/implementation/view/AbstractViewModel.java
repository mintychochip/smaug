package org.aincraft.inject.implementation.view;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import java.util.function.Function;
import org.aincraft.container.display.IViewModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AbstractViewModel<M, V, K> implements IViewModel<M, V> {

  private final Map<K, ViewModelBinding> bindings = new HashMap<>();

  private final Function<V, ViewModelBinding> viewBindingFunction;
  private final Function<M, K> modelKeyFunction;

  AbstractViewModel(Function<V, ViewModelBinding> viewBindingFunction,
      Function<M, K> modelKeyFunction) {
    this.viewBindingFunction = viewBindingFunction;
    this.modelKeyFunction = modelKeyFunction;
  }

  @Override
  public void bind(@NotNull M model, @NotNull V view) {
    final K modelKey = modelKeyFunction.apply(model);
    final ViewModelBinding binding = viewBindingFunction.apply(view);
    bindings.put(modelKey, binding);
  }

  @Override
  public void remove(@NotNull M model, Consumer<ViewModelBinding> bindingConsumer) {
    final K modelKey = modelKeyFunction.apply(model);
    final ViewModelBinding binding = bindings.remove(modelKey);
    if (bindingConsumer != null && binding != null) {
      bindingConsumer.accept(binding);
    }
  }

  @Override
  public void removeAll(@Nullable Consumer<ViewModelBinding> bindingConsumer) {
    bindings.values().forEach(binding -> {
      if (bindingConsumer != null && binding != null) {
        bindingConsumer.accept(binding);
      }
    });
    bindings.clear();
  }

  @Override
  public ViewModelBinding getBinding(M model) {
    final K key = modelKeyFunction.apply(model);
    return bindings.get(key);
  }

  @Override
  public boolean isBound(@NotNull M model) {
    final K modelKey = modelKeyFunction.apply(model);
    return bindings.containsKey(modelKey);
  }

  protected void updateBinding(M model, ViewModelBinding binding) {
    final K modelKey = modelKeyFunction.apply(model);
    bindings.put(modelKey, binding);
  }
}
