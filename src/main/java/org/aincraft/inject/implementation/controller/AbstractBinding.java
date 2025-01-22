package org.aincraft.inject.implementation.controller;

import java.lang.reflect.Field;

import org.aincraft.container.display.Binding;

public class AbstractBinding implements Binding {

    @Override
    public <T> T getProperty(String identifier, Class<T> clazz) {
        Field field = findField(identifier, this.getClass());
        ExposedProperty property = field.getAnnotation(ExposedProperty.class);
        if (property.value().equals(identifier)) {
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                return clazz.cast(value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void setProperty(String identifier, Object value) {
      Field field = findField(identifier, this.getClass());
      ExposedProperty property = field.getAnnotation(ExposedProperty.class);
      if(property.value().equals(identifier)) {
        field.setAccessible(true);

      }
    }

    @Override
    public boolean propertyIsExposed(String identifier) {
      return findField(identifier, this.getClass()) != null;
    }

    private static Field findField(String identifier, Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ExposedProperty.class)) {
                return field;
            }
        }
        return null;
    }
}
