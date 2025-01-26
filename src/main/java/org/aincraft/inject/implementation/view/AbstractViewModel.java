package org.aincraft.inject.implementation.view;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.aincraft.container.IFactory;
import org.aincraft.container.display.IViewModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AbstractViewModel<M, V, K> implements IViewModel<M, V> {

  private final Map<K, IViewModelBinding> bindings = new HashMap<>();

  private final Function<V, IViewModelBinding> viewBindingFunction;
  private final Function<M, K> modelKeyFunction;

  @NotNull
  abstract Class<? extends IViewModelBinding> getBindingClass();

  @NotNull
  abstract IFactory<V, M> getViewFactory();

  @NotNull
  abstract IViewModelBinding viewToBinding(V view);

  AbstractViewModel(Function<V, IViewModelBinding> viewBindingFunction,
      Function<M, K> modelKeyFunction) {
    this.viewBindingFunction = viewBindingFunction;
    this.modelKeyFunction = modelKeyFunction;
  }

  @Override
  public IViewModelBinding bind(@NotNull M model, @NotNull V view) {
    final K modelKey = modelKeyFunction.apply(model);
    final IViewModelBinding binding = viewBindingFunction.apply(view);
    bindings.put(modelKey, binding);
    return binding;
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
    final K modelKey = modelKeyFunction.apply(model);
    if (this.isBound(model)) {
      return bindings.get(modelKey);
    }
    final IFactory<V,M> viewFactory = this.getViewFactory();
    return this.bind(model, viewFactory.create(model));
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

  @Override
  public Map<@NotNull String, @NotNull Class<?>> getBoundedIdentifiers() {
    Class<? extends IViewModelBinding> bindingClazz = this.getBindingClass();
    Map<@NotNull String, @NotNull Class<?>> boundedIdentifiers = new HashMap<>();
    IViewModelBinding.getExposedFields(bindingClazz).forEach(((exposedProperty, field) -> {
      boundedIdentifiers.put(exposedProperty.value(), field.getType());
    }));
    return boundedIdentifiers;
  }
}
