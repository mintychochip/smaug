package org.aincraft.inject.implementation.viewmodel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import org.aincraft.container.IFactory;
import org.aincraft.container.display.IViewModel;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AbstractViewModel<M, V, K> implements IViewModel<M, V> {

    private final Map<K, IViewModelBinding> bindings = new HashMap<>();

    @NotNull
    abstract Class<? extends IViewModelBinding> getBindingClass();

    @NotNull
    abstract IFactory<V, M> getViewFactory();

    @NotNull
    abstract IViewModelBinding viewToBinding(@NotNull V view);

    @NotNull
    abstract K modelToKey(@NotNull M model);

    @Override
    public IViewModelBinding bind(@NotNull M model, @NotNull V view) {
        Preconditions.checkNotNull(model);
        Preconditions.checkNotNull(view);
        final K modelKey = modelToKey(model);
        final IViewModelBinding binding = viewToBinding(view);
        bindings.put(modelKey, binding);
        return binding;
    }

    @Override
    public void remove(@NotNull M model, @Nullable Consumer<IViewModelBinding> bindingConsumer) {
        Preconditions.checkNotNull(model);
        final K modelKey = modelToKey(model);
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
