package org.aincraft.inject.implementation.viewmodel;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.aincraft.container.gui.RefiningGuiProxy;
import org.aincraft.container.refining.RefiningPlayerStationProxy;
import org.aincraft.container.refining.RefiningSessionStore;
import org.aincraft.inject.implementation.view.RefiningGuiProxyFactory;
import org.jetbrains.annotations.NotNull;

/** Owns per-player and per-station refining GUI bindings. */
public final class RefiningGuiViewModel {

  private final Function<RefiningPlayerStationProxy, RefiningGuiProxy> factory;
  private final RefiningSessionStore sessions;
  private final Map<RefiningPlayerStationProxy.BindingKey, RefiningGuiProxy> bindings =
      new HashMap<>();

  @Inject
  public RefiningGuiViewModel(RefiningGuiProxyFactory factory, RefiningSessionStore sessions) {
    this(factory::create, sessions);
  }

  public RefiningGuiViewModel(Function<RefiningPlayerStationProxy, RefiningGuiProxy> factory,
      RefiningSessionStore sessions) {
    this.factory = Objects.requireNonNull(factory, "factory");
    this.sessions = Objects.requireNonNull(sessions, "sessions");
  }

  /** Opens a station GUI after creating its control-only session. */
  public synchronized void open(@NotNull RefiningPlayerStationProxy proxy) {
    Objects.requireNonNull(proxy, "proxy");
    sessions.open(proxy.player(), proxy.station());
    RefiningGuiProxy binding = bindings.computeIfAbsent(proxy.bindingKey(),
        ignored -> Objects.requireNonNull(factory.apply(proxy), "factory returned null"));
    binding.setCloseCleanup(() -> onGuiClosed(proxy, binding));
    binding.refresh();
    binding.open(proxy.player());
  }

  public synchronized void refresh(@NotNull RefiningPlayerStationProxy proxy) {
    Objects.requireNonNull(proxy, "proxy");
    RefiningGuiProxy binding = bindings.get(proxy.bindingKey());
    if (binding != null) {
      binding.refresh();
    }
  }

  public synchronized boolean isBound(@NotNull RefiningPlayerStationProxy proxy) {
    Objects.requireNonNull(proxy, "proxy");
    return bindings.containsKey(proxy.bindingKey());
  }

  /** Closes the view and removes its control-only session. */
  public synchronized void close(@NotNull RefiningPlayerStationProxy proxy) {
    Objects.requireNonNull(proxy, "proxy");
    RefiningGuiProxy binding = bindings.remove(proxy.bindingKey());
    if (binding != null) {
      binding.setCloseCleanup(null);
      binding.close(proxy.player());
    }
    sessions.close(proxy.player(), proxy.station());
  }

  private synchronized void onGuiClosed(RefiningPlayerStationProxy proxy,
      RefiningGuiProxy binding) {
    if (bindings.get(proxy.bindingKey()) != binding) {
      return;
    }
    bindings.remove(proxy.bindingKey());
    binding.setCloseCleanup(null);
    sessions.close(proxy.player(), proxy.station());
  }
}
