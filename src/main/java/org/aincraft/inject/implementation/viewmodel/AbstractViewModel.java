/*
 *
 * Copyright (C) 2025 mintychochip
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.aincraft.inject.implementation.viewmodel;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.aincraft.container.IFactory;
import org.aincraft.container.display.IViewModel;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AbstractViewModel<M, V, K> implements IViewModel<M> {

  private final Map<K, IViewModelBinding> bindings = new HashMap<>();

  @NotNull
  abstract Class<? extends IViewModelBinding> getBindingClass();

  @NotNull
  abstract IFactory<V, M> getViewFactory();

  @NotNull
  abstract IViewModelBinding viewToBinding(@NotNull V view);

  @NotNull
  abstract K modelToKey(@NotNull M model);

  public IViewModelBinding bind(@NotNull M model, @NotNull V view) {
    Preconditions.checkNotNull(model);
    Preconditions.checkNotNull(view);
    final K modelKey = modelToKey(model);
    final IViewModelBinding binding = viewToBinding(view);
    bindings.put(modelKey, binding);
    return binding;
  }

  @Override
  public IViewModelBinding remove(@NotNull M model) {
    Preconditions.checkNotNull(model);
    final K modelKey = modelToKey(model);
    return bindings.remove(modelKey);
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
  public IViewModelBinding getBinding(@NotNull M model) {
    Preconditions.checkNotNull(model);
    final K modelKey = modelToKey(model);
    if (this.isBound(model)) {
      return bindings.get(modelKey);
    }
    final IFactory<V, M> viewFactory = this.getViewFactory();
    return this.bind(model, viewFactory.create(model));
  }

  @Override
  public boolean isBound(@NotNull M model) {
    Preconditions.checkNotNull(model);
    final K modelKey = modelToKey(model);
    return bindings.containsKey(modelKey);
  }

  @ApiStatus.Experimental
  protected void updateBinding(@NotNull M model, @NotNull IViewModelBinding binding) {
    Preconditions.checkNotNull(model);
    final K modelKey = modelToKey(model);
    bindings.put(modelKey, binding);
  }

  @Override
  public Map<@NotNull String, @NotNull Class<?>> getBoundedIdentifiers() {
    final Class<? extends IViewModelBinding> bindingClazz = this.getBindingClass();
    Map<@NotNull String, @NotNull Class<?>> boundedIdentifiers = new HashMap<>();
    IViewModelBinding.getExposedFields(bindingClazz).forEach(((exposedProperty, field) -> {
      boundedIdentifiers.put(exposedProperty.value(), field.getType());
    }));
    return boundedIdentifiers;
  }
}
