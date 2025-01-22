package org.aincraft.container.display;

import org.aincraft.container.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface IViewModel<M, V, K> {

    void bind(@NotNull M model, @NotNull V view);

    void update(@NotNull M model, @NotNull Object... data);

    void remove(@NotNull Object modelKey, @Nullable Consumer<ViewModelBinding> bindingConsumer);

    default void remove(@NotNull Object modelKey) {
      remove(modelKey, null);
    }

    default void removeAll() {
      removeAll(null);
    }

    void removeAll(@Nullable Consumer<ViewModelBinding> bindingConsumer);

    boolean isBound(@NotNull Object modelKey);

    ViewModelBinding getBinding(M model);
}
