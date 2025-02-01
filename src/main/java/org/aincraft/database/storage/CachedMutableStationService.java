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
import net.kyori.adventure.key.Key;
import org.aincraft.database.model.MutableStation;
import org.aincraft.database.model.meta.Meta;
import org.aincraft.database.model.meta.Meta.MetaMapping;
import org.aincraft.database.model.test.IStation;
import org.aincraft.listener.IMutableStationService;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CachedMutableStationService<M extends Meta<M>> implements
    IMutableStationService<M> {

  private final Cache<UUID, MutableStation<M>> stationCache = Caffeine.newBuilder().expireAfterWrite(
      Duration.ofMinutes(5)).build();

  private final Cache<Location, MutableStation<M>> stationLocationCache = Caffeine.newBuilder()
      .expireAfterWrite(Duration.ofMinutes(5)).build();

  private final IStorage storage;

  private final MetaMapping<M> metaMapping;

  public CachedMutableStationService(IStorage storage, MetaMapping<M> metaMapping) {
    this.storage = storage;
    this.metaMapping = metaMapping;
  }

  @Nullable
  @Override
  public MutableStation<M> getStation(@NotNull Location location) {
    Preconditions.checkNotNull(location);
    MutableStation<M> mutableStation = stationLocationCache.get(location, k -> {
      IStation station = storage.getStation(k);
      MutableStation.create()
    });
    if(mutableStation == null) {
      return null;
    }
    stationCache.put(mutableStation.id(), mutableStation);
    return mutableStation;
  }

  @Override
  public @Nullable MutableStation<M> getStation(@NotNull UUID stationId) {
    Preconditions.checkNotNull(stationId);
    final String idString = stationId.toString();
    MutableStation<M> mutableStation = stationCache.get(stationId, k -> {
      M meta = metaMapping.getMeta(idString);
      return executor.queryRow(scanner -> {
        try {
          String keyString = scanner.getString("station_key");
          String worldName = scanner.getString("world_name");
          int x = scanner.getInt("x");
          int y = scanner.getInt("y");
          int z = scanner.getInt("z");
          return MutableStation.create(idString, keyString, worldName, x, y, z, meta);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }, GET_STATION_BY_ID, idString);
    });
    if (mutableStation == null) {
      return null;
    }
    stationLocationCache.put(mutableStation.blockLocation(), mutableStation);
    return mutableStation;
  }

  @Override
  public MutableStation<M> createStation(Key stationKey, Location location) {
    String idString = UUID.randomUUID().toString();
    String keyString = stationKey.toString();
    String worldName = location.getWorld().getName();
    int x = location.getBlockX();
    int y = location.getBlockY();
    int z = location.getBlockZ();
    executor.executeUpdate(CREATE_STATION, idString, keyString, worldName, x, y, z);
    final M meta = metaMapping.createMeta(idString);
    return MutableStation.create(idString, keyString, worldName, x, y, z, meta);
  }

  @Override
  public void updateStation(MutableStation<M> mutableStation) {

  }

  @Override
  public void deleteStation(Location location) {

  }
}
