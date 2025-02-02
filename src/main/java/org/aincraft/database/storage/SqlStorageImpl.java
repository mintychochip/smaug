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

import com.google.common.base.Preconditions;
import java.sql.SQLException;
import java.util.UUID;
import org.aincraft.database.model.test.IStation;
import org.bukkit.Location;

public class SqlStorageImpl implements IStorage {

  private static final String GET_STATION_BY_ID = "SELECT station_key,world_name,x,y,z FROM stations WHERE id=?";
  private static final String GET_STATION_BY_LOCATION = "SELECT id,station_key FROM stations WHERE world_name=? AND x=? AND y=? AND z=?";
  private static final String CREATE_STATION = "INSERT INTO stations (id,station_key,world_name,x,y,z) VALUES (?,?,?,?,?,?)";
  private static final String REMOVE_STATION_BY_LOCATION = "DELETE FROM stations WHERE world_name=? AND x=? AND y=? AND z=?";

  private final IConnectionSource source;
  private final SqlExecutor executor;

  public SqlStorageImpl(IConnectionSource source) {
    this.source = source;
    this.executor = new SqlExecutor(source);
  }

  @Override
  public IStation getStation(String idString) {
    Preconditions.checkNotNull(idString);
    return executor.queryRow(scanner -> {
      try {
        String keyString = scanner.getString("station_key");
        String worldName = scanner.getString("world_name");
        int x = scanner.getInt("x");
        int y = scanner.getInt("y");
        int z = scanner.getInt("z");
        return IStation.create(idString, keyString, worldName, x, y, z);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }, GET_STATION_BY_ID, idString);
  }

  @Override
  public IStation getStation(Location location) {
    Preconditions.checkNotNull(location);
    final String worldName = location.getWorld().getName();
    final int x = location.getBlockX();
    final int y = location.getBlockY();
    final int z = location.getBlockZ();
    return executor.queryRow(scanner -> {
      try {
        String idString = scanner.getString("id");
        String keyString = scanner.getString("station_key");
        return IStation.create(idString, keyString, worldName, x, y, z);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, GET_STATION_BY_LOCATION,worldName,x,y,z);
  }

  @Override
  public IStation createStation(String keyString, Location location) {
    Preconditions.checkNotNull(keyString);
    Preconditions.checkNotNull(location);
    final String idString = UUID.randomUUID().toString();
    final String worldName = location.getWorld().getName();
    final int x = location.getBlockX();
    final int y = location.getBlockY();
    final int z = location.getBlockZ();
    executor.executeUpdate(CREATE_STATION, idString, keyString, worldName, x, y, z);
    return IStation.create(idString,keyString,worldName,x,y,z);
  }

  @Override
  public void removeStation(Location location) {
    Preconditions.checkNotNull(location);
    final String worldName = location.getWorld().getName();
    final int x = location.getBlockX();
    final int y = location.getBlockY();
    final int z = location.getBlockZ();
    executor.executeUpdate(REMOVE_STATION_BY_LOCATION,worldName, x, y, z);
  }

  @Override
  public SqlExecutor getExecutor() {
    return executor;
  }

  @Override
  public void close() {
    try {
      source.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
