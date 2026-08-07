package org.aincraft.inject.implementation.viewmodel;

import com.google.inject.Inject;
import java.util.Objects;
import java.util.function.Function;
import org.aincraft.container.display.ViewModel;
import org.aincraft.container.gui.RefiningGuiProxy;
import org.aincraft.container.refining.RefiningPlayerStationProxy;
import org.aincraft.container.refining.RefiningSessionStore;
import org.aincraft.inject.implementation.view.RefiningGuiProxyFactory;
import org.jetbrains.annotations.NotNull;

/** Owns per-player and per-station refining GUI bindings. */
public final class RefiningGuiViewModel
    extends ViewModel<RefiningPlayerStationProxy, RefiningGuiProxy> {

  private final Function<RefiningPlayerStationProxy, RefiningGuiProxy> factory;
  private final RefiningSessionStore sessions;

  @Inject
  public RefiningGuiViewModel(RefiningGuiProxyFactory factory, RefiningSessionStore sessions) {
    this(factory::create, sessions);
  }

  public RefiningGuiViewModel(Function<RefiningPlayerStationProxy, RefiningGuiProxy> factory,
      RefiningSessionStore sessions) {
    this.factory = Objects.requireNonNull(factory, "factory");
    this.sessions = Objects.requireNonNull(sessions, "sessions");
  }

  @Override
  protected @NotNull Object keyOf(@NotNull RefiningPlayerStationProxy model) {
    return model.bindingKey();
  }

  @Override
  protected @NotNull RefiningGuiProxy createBinding(@NotNull RefiningPlayerStationProxy model) {
    return Objects.requireNonNull(factory.apply(model), "factory returned null");
  }

  /** Opens a station GUI after creating its control-only session. */
  public void open(@NotNull RefiningPlayerStationProxy proxy) {
    sessions.open(proxy.player(), proxy.station());
    RefiningGuiProxy binding = getBinding(proxy);
    binding.refresh();
    binding.open(proxy.player());
  }

  @Override
  public void update(@NotNull RefiningPlayerStationProxy model) {
    RefiningGuiProxy binding = findBinding(model);
    if (binding != null) {
      binding.refresh();
    }
  }

  public void refresh(@NotNull RefiningPlayerStationProxy proxy) {
    updateIfBound(proxy);
  }

  /** Closes the view and removes its control-only session. */
  public void close(@NotNull RefiningPlayerStationProxy proxy) {
    RefiningGuiProxy binding = findBinding(proxy);
    if (binding != null) {
      binding.close(proxy.player());
      remove(proxy);
    }
    sessions.close(proxy.player(), proxy.station());
  }
}
