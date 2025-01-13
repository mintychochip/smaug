package org.aincraft.listener;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import java.util.Map;
import org.aincraft.container.StationHandler;
import org.bukkit.NamespacedKey;

public class StationModule extends AbstractModule {

  private final Map<NamespacedKey, StationHandler> handlers;


  public StationModule(Map<NamespacedKey, StationHandler> handlers) {
    this.handlers = handlers;
  }

  @Override
  protected void configure() {
    this.bind(new TypeLiteral<Map<NamespacedKey, StationHandler>>() {
    }).toInstance(handlers);
  }
}
