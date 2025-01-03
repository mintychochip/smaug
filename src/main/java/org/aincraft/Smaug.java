package org.aincraft;

import org.aincraft.container.InteractionKey;
import org.aincraft.container.StationHandler;

public interface Smaug {

  void registerStationHandler(InteractionKey key,
      StationHandler handler);
}
