/*
 * MIT License
 *
 * Copyright (c) 2025 mintychochip
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * provided to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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

  IViewModelBinding getBinding(@NotNull M model);

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
