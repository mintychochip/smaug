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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import org.aincraft.database.model.test.IStation;
import org.aincraft.listener.IStationDatabaseService;
import org.bukkit.Location;

public final class CachedStationDatabaseService implements IStationDatabaseService {

  private final Cache<UUID, IStation> stationCache = Caffeine.newBuilder().expireAfterWrite(
      Duration.ofMinutes(1)).build();

  private final Cache<Location, IStation> stationLocationCache = Caffeine.newBuilder()
      .expireAfterWrite(Duration.ofMinutes(1)).build();
  private final IStorage storage;

  public CachedStationDatabaseService(IStorage storage) {
    this.storage = storage;
  }

  @Override
  public void removeStation(IStation station) {
    stationCache.invalidate(station.getId());
    stationLocationCache.invalidate(station.getBlockLocation());
    storage.removeStation(station.getBlockLocation());
  }

  @Override
  public IStation createStation(Key stationKey, Location location) {
    IStation station = storage.createStation(stationKey.toString(), location);
    stationCache.put(station.getId(), station);
    stationLocationCache.put(location, station);
    return station;
  }

  @Override
  public IStation getStation(Location location) {
    return stationLocationCache.get(location,k -> storage.getStation(location));
  }

  @Override
  public IStation getStation(UUID stationId) {
    return stationCache.get(stationId,k -> storage.getStation(k.toString()));
  }
}
