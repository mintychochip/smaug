package org.aincraft.container.display;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface Binding {

  <T> T getProperty(String identifier, Class<T> clazz);

  void setProperty(String identifier, Object value);

  boolean isPropertyExposed(String identifier);

  @Retention(RetentionPolicy.RUNTIME)
  @interface ExposedProperty {

    String value();
  }
}
