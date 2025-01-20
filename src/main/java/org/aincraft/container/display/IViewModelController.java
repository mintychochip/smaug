package org.aincraft.container.display;

import java.util.Collection;
import net.kyori.adventure.key.Key;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public interface IViewModelController<M, V> extends Iterable<IViewModel<M, V>>, Listener {

  void register(@NotNull Key stationKey, @NotNull IViewModel<M, V> viewModel);

  boolean isRegistered(@NotNull Key stationKey);

  IViewModel<M, V> get(@NotNull Key stationKey);

  Collection<IViewModel<M, V>> getAll();
}
