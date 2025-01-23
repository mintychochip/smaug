package org.aincraft.container.display;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface IViewModel<M, V> {

  void bind(@NotNull M model, @NotNull V view);

  void update(@NotNull M model, @NotNull Object... data);

  void remove(@NotNull M model, @Nullable Consumer<IViewModelBinding> bindingConsumer);

  default void remove(@NotNull M model) {
    remove(model, null);
  }

  default void removeAll() {
    removeAll(null);
  }

  void removeAll(@Nullable Consumer<IViewModelBinding> bindingConsumer);

  boolean isBound(@NotNull M model);

  IViewModelBinding getBinding(M model);

  interface IViewModelBinding {

    @NotNull
    <T> T getProperty(@NotNull String identifier, @NotNull Class<T> clazz)
        throws PropertyNotFoundException;

    @Nullable
    <T> T getProperty(@NotNull Class<T> clazz);

    void setProperty(@NotNull String identifier, @NotNull Object value)
        throws PropertyNotFoundException;

    boolean isPropertyExposed(String identifier);

    Map<@NotNull ExposedProperty, Object> getExposedProperties();

    @Retention(RetentionPolicy.RUNTIME)
    @interface ExposedProperty {

      String value();
    }
  }
}
