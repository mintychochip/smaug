package org.aincraft.inject;

import org.aincraft.container.IRegistry;
import org.aincraft.container.item.IKeyedItem;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IItemParser {

  @Nullable
  IKeyedItem parse(@Nullable ConfigurationSection section, @NotNull IRegistry.IItemRegistry registry);
}
