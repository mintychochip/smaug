package org.aincraft.listener;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import java.util.Map;
import net.kyori.adventure.key.Key;
import org.aincraft.container.StationHandler;
import org.bukkit.NamespacedKey;

public class StationModule extends AbstractModule {

  private final Map<Key, StationHandler> handlers;


  public StationModule(Map<Key, StationHandler> handlers) {
    this.handlers = handlers;
  }

  @Override
  protected void configure() {
    this.bind(new TypeLiteral<Map<Key, StationHandler>>() {
    }).toInstance(handlers);
  }
}
