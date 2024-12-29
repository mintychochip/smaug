package org.aincraft.inject.provider;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IParser<T,R> {

  @Nullable
  T parse(@Nullable ConfigurationSection section, @NotNull R registry);
}
