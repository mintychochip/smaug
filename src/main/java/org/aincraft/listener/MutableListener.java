/*
 *
 * Copyright (C) 2025 mintychochip
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.aincraft.listener;

import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.database.model.meta.IMeta;
import org.aincraft.database.model.test.IMetaStation;
import org.aincraft.database.storage.CachedMutableStationDatabaseService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MutableListener<M extends IMeta<M>, E extends StationUpdateEvent<M>> implements Listener {

  private final IMetaStationDatabaseService<M> stationService;

  public MutableListener(IMetaStationDatabaseService<M> stationService) {
    this.stationService = stationService;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleUpdate(final E event) {
    if (event.isCancelled()) {
      return;
    }
    IMetaStation<M> station = event.getStation();
    stationService.updateStation(station);
  }
}
