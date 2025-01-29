/*
 * MIT License
 *
 * Copyright (c) 2025 mintychochip
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * provided to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.aincraft.database.storage;

import java.util.List;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationUser;

public interface IStorage {

  //  // Table-related methods
  List<Station> getAllStations();

  void updateStation(Station model);

  Station createStation(String stationKey, String worldName, int x, int y, int z);

  void deleteStation(String worldName, int x, int y, int z);

  Station getStation(String worldName, int x, int y, int z);

  Station getStation(String stationId);

  boolean hasStationUser(String playerId);

  StationUser createStationUser(String playerId, String playerName);

  StationUser getStationUser(String playerId);

  boolean updateStationUser(StationUser user);

  void close();
}
