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
import java.util.UUID;
import net.kyori.adventure.key.Key;
import org.aincraft.database.model.test.IMetaStation;
import org.aincraft.database.model.test.IStation;
import org.aincraft.handler.IStationHandler;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StationServiceLocator {

  private final Map<Key, IStationFacade> services;
  private final IStationDatabaseService databaseService;

  private StationServiceLocator(Map<Key, IStationFacade> services,
      IStationDatabaseService databaseService) {
    this.services = services;
    this.databaseService = databaseService;
  }

  public StationServiceLocator(
      IStationDatabaseService databaseService) {
    this(new HashMap<>(), databaseService);
  }

  @Nullable
  public StationServiceLocator.IStationFacade getServices(Key key) {
    return services.get(key);
  }

  @Nullable
  public StationServiceLocator.IStationFacade getServices(Location location) {
    final IStation station = databaseService.getStation(location);
    if (station == null) {
      return null;
    }
    return services.get(station.getKey());
  }

  public interface IStationFacade {

    IStationHandler getHandler();

    void removeStation(IStation station);

    IStation createStation(Key stationKey, Location location);

    IStation getStation(Location location);

    IStation getStation(UUID stationId);

    void updateStation(IStation station);

//    <M extends Meta<M>> IMetaStation<M> getStation(Location location, Class<M> metaClass);
  }

  public static class StationFacadeImpl implements IStationFacade {

    protected final IStationDatabaseService databaseService;
    private final IStationHandler handler;

    public StationFacadeImpl(@NotNull IStationHandler handler,
        @NotNull IStationDatabaseService databaseService) {
      this.handler = handler;
      this.databaseService = databaseService;
    }

    @Override
    public IStationHandler getHandler() {
      return handler;
    }

    @Override
    public void removeStation(IStation station) {
      databaseService.removeStation(station);
    }

    @Override
    public IStation createStation(Key stationKey, Location location) {
      return databaseService.createStation(stationKey, location);
    }

    @Override
    public IStation getStation(Location location) {
      return databaseService.getStation(location);
    }

    @Override
    public IStation getStation(UUID stationId) {
      return databaseService.getStation(stationId);
    }

    @Override
    public void updateStation(IStation station) {
      if(!(station instanceof IMetaStation<?> metaStation)) {
        return;
      }
      IMetaStationDatabaseService<?> databaseService1 = (IMetaStationDatabaseService<?>) databaseService;
    }

//    @Override
//    public <M extends Meta<M>> IMetaStation<M> getStation(Location location, Class<M> metaClass) {
//      IStation station = this.getStation(location);
//      if (!(station instanceof IMetaStation<?> metaStation)) {
//        return null;
//      }
//      if (!metaClass.isAssignableFrom(metaStation.getMeta().getClass())) {
//        return null;
//      }
//      @SuppressWarnings("unchecked")
//      IMetaStation<M> mStation = (IMetaStation<M>) metaStation;
//      return mStation;
//    }
  }

  public static final class Builder {

    private final Map<Key, IStationFacade> services;
    private final IStationDatabaseService databaseService;

    private Builder(Map<Key, IStationFacade> services, IStationDatabaseService databaseService) {
      this.services = services;
      this.databaseService = databaseService;
    }

    public Builder(IStationDatabaseService databaseService) {
      this(new HashMap<>(), databaseService);
    }

    public Builder setService(Key key, IStationFacade services) {
      this.services.put(key, services);
      return this;
    }

    public StationServiceLocator build() {
      return new StationServiceLocator(services, databaseService);
    }
  }
}
