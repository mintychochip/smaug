package org.aincraft.container;

import org.aincraft.api.event.StationInteractEvent;
import org.aincraft.listener.StationService;

public interface StationHandler {
  void handle(final StationInteractEvent event, final StationService service);
}
