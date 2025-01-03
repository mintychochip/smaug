package org.aincraft.container;

import org.aincraft.api.event.StationInteractEvent;
import org.aincraft.listener.IStationService;

public interface StationHandler {
  void handle(final StationInteractEvent event, final IStationService service);
}
