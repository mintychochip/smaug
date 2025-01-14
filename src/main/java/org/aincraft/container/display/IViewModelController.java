package org.aincraft.container.display;

import java.util.UUID;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface IViewModelController {
  void register(@NotNull Key key, @NotNull IViewModel viewModel);
  boolean isRegistered(@NotNull Key key);
  IViewModel get(@NotNull Key key);
  void update(UUID stationId);
}
