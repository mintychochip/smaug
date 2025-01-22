package org.aincraft.inject.implementation.controller;

import java.lang.reflect.Field;
import org.aincraft.container.display.Binding;

public class AbstractBinding implements Binding {

  @Override
  public <T> T getProperty(String identifier, Class<T> clazz) {
    Field[] fields = this.getClass().getDeclaredFields();
    for(Field field : fields) {
      if(field.isAnnotationPresent(ExposedProperty.class)) {
        ExposedProperty property = field.getAnnotation(ExposedProperty.class);
        if(property.value().equals(identifier)) {
          field.setAccessible(true);
          try {
            Object value = field.get(this);
            return clazz.cast(value);
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
    return null;
  }
}
