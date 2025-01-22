package org.aincraft.inject.implementation.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.aincraft.container.display.IViewModel;
import org.aincraft.container.display.ViewModelBinding;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractViewModel<M, V, K> implements IViewModel<M, V> {

    protected final Map<K, ViewModelBinding> bindings = new HashMap<>();

    @NotNull
    protected abstract ViewModelBinding viewToBinding(V view);

    @NotNull
    protected abstract K getModelKey(M model);

    @Override
    public void bind(@NotNull M model, @NotNull V view) {
        final K modelKey = getModelKey(model);
        final ViewModelBinding binding = viewToBinding().apply(view);
        bindings.put(modelKey, binding);
    }

    @Override
    public void remove(@NotNull Object modelKey, @Nullable Consumer<ViewModelBinding> bindingConsumer) {
        final ViewModelBinding binding = bindings.remove((K) modelKey);
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
        final K key = this.getModelKey(model);
        return bindings.get(key);
    }

    @Override
    public boolean isBound(@NotNull Object modelKey) {
        return false;
    }
}
