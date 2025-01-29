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

package org.aincraft.inject.implementation.viewmodel;

import com.google.common.base.Preconditions;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.aincraft.container.display.IViewModel.IViewModelBinding;
import org.aincraft.container.display.PropertyNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AbstractBinding implements IViewModelBinding {

  @Override
  public <T> @NotNull T getProperty(@NotNull String identifier, @NotNull Class<T> clazz)
      throws PropertyNotFoundException {
    Preconditions.checkNotNull(identifier);
    Preconditions.checkNotNull(clazz);
    Field field = findField(identifier, this.getClass());
    if (field == null) {
      throw new PropertyNotFoundException(identifier);
    }
    field.setAccessible(true);
    try {
      Object value = field.get(this);
      return clazz.cast(value);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> @Nullable T getProperty(@NotNull Class<T> clazz) {
    Preconditions.checkNotNull(clazz);
    Field[] fields = this.getClass().getDeclaredFields();
    for (Field field : fields) {
      if (field.getType().equals(clazz)) {
        field.setAccessible(true);
        try {
          Object value = field.get(this);
          return clazz.cast(value);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return null;
  }

  @Override
  public void setProperty(@NotNull String identifier, @NotNull Object value)
      throws PropertyNotFoundException {
    Preconditions.checkNotNull(identifier);
    Preconditions.checkNotNull(value);
    Field field = findField(identifier, this.getClass());
    if (field == null) {
      throw new PropertyNotFoundException(identifier);
    }
    field.setAccessible(true);
    try {
      field.set(this, value);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isPropertyExposed(String identifier) {
    return findField(identifier, this.getClass()) != null;
  }

  @Override
  public Map<@NotNull ExposedProperty, Object> getExposedProperties() {
    Map<@NotNull ExposedProperty, Field> fields = IViewModelBinding.getExposedFields(
        this.getClass());
    Map<@NotNull ExposedProperty, Object> result = new HashMap<>();
    for (Map.Entry<ExposedProperty, Field> entry : fields.entrySet()) {
      Field field = entry.getValue();
      field.setAccessible(true);
      try {
        Object fieldValue = field.get(this);
        result.put(entry.getKey(), fieldValue);
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Unable to access field: " + field.getName(), e);
      }
    }

    return result;
  }

  private static Field findField(String identifier, Class<?> clazz) {
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      if (field.isAnnotationPresent(ExposedProperty.class)) {
        String propertyIdentifier = field.getAnnotation(ExposedProperty.class).value();
        if (propertyIdentifier.equals(identifier)) {
          return field;
        }
      }
    }
    return null;
  }
}
