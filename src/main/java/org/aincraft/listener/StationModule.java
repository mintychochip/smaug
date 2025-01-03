package org.aincraft.listener;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import java.util.Map;
import org.aincraft.container.InteractionKey;
import org.aincraft.container.StationHandler;

public class StationModule extends AbstractModule {

  private final Map<InteractionKey, StationHandler> handlers;


  public StationModule(Map<InteractionKey, StationHandler> handlers) {
    this.handlers = handlers;
  }

  @Override
  protected void configure() {
    this.bind(new TypeLiteral<Map<InteractionKey, StationHandler>>() {
    }).toInstance(handlers);
  }
}
