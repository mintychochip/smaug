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
