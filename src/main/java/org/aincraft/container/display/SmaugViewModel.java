package org.aincraft.container.display;

import java.util.Collection;
import java.util.UUID;
import org.aincraft.database.model.Station;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface SmaugViewModel {
  void bind(@NotNull Station station, @NotNull SmaugView view);
  void update(@NotNull UUID stationId, @NotNull Collection<ItemStack> stacks);
  void remove(@NotNull UUID stationId);
  boolean isBound(@NotNull UUID stationId);
}
