package org.aincraft.inject.implementation.viewmodel;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
  private final Map<RefiningPlayerStationProxy.BindingKey, Binding> bindings =
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
    Binding binding = bindings.computeIfAbsent(proxy.bindingKey(),
        ignored -> new Binding(proxy,
            Objects.requireNonNull(factory.apply(proxy), "factory returned null")));
    binding.gui().setCloseCleanup(() -> onGuiClosed(binding));
    binding.gui().refresh();
    binding.gui().open(proxy.player());
  }

  public synchronized void refresh(@NotNull RefiningPlayerStationProxy proxy) {
    Objects.requireNonNull(proxy, "proxy");
    Binding binding = bindings.get(proxy.bindingKey());
    if (binding != null) {
      binding.gui().refresh();
    }
  }

  public synchronized boolean isBound(@NotNull RefiningPlayerStationProxy proxy) {
    Objects.requireNonNull(proxy, "proxy");
    return bindings.containsKey(proxy.bindingKey());
  }

  /** Closes the view and removes its control-only session. */
  public synchronized void close(@NotNull RefiningPlayerStationProxy proxy) {
    Objects.requireNonNull(proxy, "proxy");
    Binding binding = bindings.remove(proxy.bindingKey());
    if (binding != null) {
      binding.gui().setCloseCleanup(null);
      binding.gui().close(binding.proxy().player());
    }
    sessions.close(proxy.player(), proxy.station());
  }

  /** Closes every open refining view and drops every control-only session. */
  public synchronized void clearAll() {
    List<Binding> active = new ArrayList<>(bindings.values());
    bindings.clear();
    try {
      for (Binding binding : active) {
        binding.gui().setCloseCleanup(null);
        binding.gui().close(binding.proxy().player());
      }
    } finally {
      sessions.clearAll();
    }
  }

  private synchronized void onGuiClosed(Binding binding) {
    if (bindings.get(binding.proxy().bindingKey()) != binding) {
      return;
    }
    bindings.remove(binding.proxy().bindingKey());
    binding.gui().setCloseCleanup(null);
    sessions.close(binding.proxy().player(), binding.proxy().station());
  }

  private record Binding(RefiningPlayerStationProxy proxy, RefiningGuiProxy gui) {
  }
}
