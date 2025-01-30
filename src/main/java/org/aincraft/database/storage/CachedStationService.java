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
import com.google.common.base.Preconditions;
import java.sql.SQLException;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Function;
import net.kyori.adventure.key.Key;
import org.aincraft.container.IParameterizedFactory;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.meta.Meta;
import org.aincraft.listener.IStationService;
import org.bukkit.Location;

public class CachedStationService<M extends Meta<M>> implements IStationService<M> {

  private final Cache<Location, Station<M>> stationCache = Caffeine.newBuilder().expireAfterWrite(
      Duration.ofMinutes(5)).build();

  private final IConnectionSource source;

  private final SqlExecutor executor;

  private static final String CREATE_STATION = "INSERT INTO stations (id,station_key,world_name,x,y,z) VALUES (?,?,?,?,?,?)";

  private static final String GET_STATION_BY_LOCATION = "SELECT id,station_key FROM stations WHERE world_name=? AND x=? AND y=? AND z=?";
  private final Function<SqlExecutor, M> createMetaFunction;
  private final Function<SqlExecutor, M> getMetaFunction;

  public CachedStationService(IConnectionSource source, Function<SqlExecutor, M> createMetaFunction,
      Function<SqlExecutor, M> getMetaFunction) {
    this.source = source;
    this.executor = new SqlExecutor(source);
    this.createMetaFunction = createMetaFunction;
    this.getMetaFunction = getMetaFunction;
  }

  public Station<M> getStation(Location location) {
    Preconditions.checkNotNull(location);
    return stationCache.get(location, key -> {
      M meta = getMetaFunction.apply(this.executor);
      String worldName = location.getWorld().getName();
      int x = location.getBlockX();
      int y = location.getBlockY();
      int z = location.getBlockZ();
      return executor.queryRow(scanner -> {
        try {
          String idString = scanner.getString("id");
          String keyString = scanner.getString("station_key");
          return Station.create(idString, keyString, worldName, x, y, z, meta);
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }, GET_STATION_BY_LOCATION);
    });
  }

  @Override
  public Station<M> getStation(UUID stationId) {
    return null;
  }

  @Override
  public Station<M> createStation(Key stationKey, Location location) {
    String idString = UUID.randomUUID().toString();
    String keyString = stationKey.toString();
    String worldName = location.getWorld().getName();
    int x = location.getBlockX();
    int y = location.getBlockY();
    int z = location.getBlockZ();
    executor.executeUpdate(CREATE_STATION, idString, keyString, worldName, x, y, z);
    M meta = createMetaFunction.apply(this.executor);
    return Station.create(idString, keyString, worldName, x, y, z, meta);
  }

  @Override
  public void updateStation(Station<M> station) {

  }

  @Override
  public void deleteStation(Location location) {

  }
}
