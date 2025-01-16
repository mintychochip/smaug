package org.aincraft.container.display;

import java.util.Collection;
import java.util.UUID;
import org.aincraft.database.model.Station;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IViewModel {

  void bind(@NotNull Station station, @NotNull View view);

  void update(@NotNull UUID stationId, @NotNull Collection<ItemStack> stacks);

  void remove(@NotNull UUID stationId);

  void removeAll();

  boolean isBound(@NotNull UUID stationId);
}
