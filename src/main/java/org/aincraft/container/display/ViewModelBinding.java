package org.aincraft.container.display;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

public interface ViewModelBinding {

    @NotNull
    <T> T getProperty(@NotNull String identifier, @NotNull Class<T> clazz) throws PropertyNotFoundException;

    @Nullable
    <T> T getProperty(@NotNull Class<T> clazz);

    void setProperty(@NotNull String identifier, @NotNull Object value) throws PropertyNotFoundException;

    boolean isPropertyExposed(String identifier);

    Map<@NotNull ExposedProperty,Object> getExposedProperties();

    @Retention(RetentionPolicy.RUNTIME)
    @interface ExposedProperty {

        String value();
    }
}
