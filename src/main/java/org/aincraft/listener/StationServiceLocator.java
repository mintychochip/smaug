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

import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.key.Key;
import org.aincraft.database.model.test.IStation;
import org.aincraft.handler.IStationHandler;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StationServiceLocator {

  private final Map<Key, StationServices> services;
  private final IStationDatabaseService databaseService;

  private StationServiceLocator(Map<Key, StationServices> services,
      IStationDatabaseService databaseService) {
    this.services = services;
    this.databaseService = databaseService;
  }

  public StationServiceLocator(
      IStationDatabaseService databaseService) {
    this(new HashMap<>(), databaseService);
  }

  @Nullable
  public StationServices getServices(Key key) {
    return services.get(key);
  }

  @Nullable
  public StationServices getServices(Location location) {
    final IStation station = databaseService.getStation(location);
    if (station == null) {
      return null;
    }
    return services.get(station.getKey());
  }


  public static final class StationServices {

    private final IStationDatabaseService databaseService;
    private final IStationHandler handler;

    public StationServices(@NotNull IStationHandler handler,
        @NotNull IStationDatabaseService databaseService) {
      this.handler = handler;
      this.databaseService = databaseService;
    }

    public IStationHandler getHandler() {
      return handler;
    }

    public IStationDatabaseService getDatabaseService() {
      return databaseService;
    }
  }

  public static final class Builder {

    private final Map<Key, StationServices> services;
    private final IStationDatabaseService databaseService;

    private Builder(Map<Key, StationServices> services, IStationDatabaseService databaseService) {
      this.services = services;
      this.databaseService = databaseService;
    }

    public Builder(IStationDatabaseService databaseService) {
      this(new HashMap<>(), databaseService);
    }

    public Builder setService(Key key, StationServices services) {
      this.services.put(key, services);
      return this;
    }

    public StationServiceLocator build() {
      return new StationServiceLocator(services, databaseService);
    }
  }
}
