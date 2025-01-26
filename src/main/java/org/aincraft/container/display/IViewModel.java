package org.aincraft.container.display;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IViewModel<M, V> {

  IViewModelBinding bind(@NotNull M model, @NotNull V view);

  void update(@NotNull M model);

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

  Map<@NotNull String, @NotNull Class<?>> getBoundedIdentifiers();

  interface IViewModelBinding {

    @NotNull
    <T> T getProperty(@NotNull String identifier, @NotNull Class<T> clazz)
        throws PropertyNotFoundException;

    @Nullable
    <T> T getProperty(@NotNull Class<T> clazz);

    void setProperty(@NotNull String identifier, @NotNull Object value)
        throws PropertyNotFoundException;

    static Map<@NotNull ExposedProperty, @NotNull Field> getExposedFields(
        Class<? extends IViewModelBinding> bindingClazz) {
      Field[] fields = bindingClazz.getDeclaredFields();
      Map<ExposedProperty, Field> exposedProperties = new HashMap<>();
      for (Field field : fields) {
        if (field.isAnnotationPresent(ExposedProperty.class)) {
          ExposedProperty exposedProperty = field.getAnnotation(ExposedProperty.class);
          exposedProperties.put(exposedProperty, field);
        }
      }
      return exposedProperties;
    }

    boolean isPropertyExposed(String identifier);

    Map<@NotNull ExposedProperty, Object> getExposedProperties();

    @Retention(RetentionPolicy.RUNTIME)
    @interface ExposedProperty {

      String value();
    }
  }
}
