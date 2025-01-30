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

package org.aincraft.database.storage;

import java.util.List;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationUser;
import org.aincraft.database.model.meta.TrackableProgressMeta;

public interface IStorage {

  //  // Table-related methods

  void updateStation(Station<?> model);

  Station<?> createStation(String stationKey, String worldName, int x, int y, int z);

  void deleteStation(String worldName, int x, int y, int z);

  Station getStation(String worldName, int x, int y, int z);

  Station getStation(String stationId);

  boolean hasStationUser(String playerId);

  StationUser createStationUser(String playerId, String playerName);

  StationUser getStationUser(String playerId);

  boolean updateStationUser(StationUser user);

  IConnectionSource getSource();

  void close();
}
