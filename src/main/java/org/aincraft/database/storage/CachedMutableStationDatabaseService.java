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
import java.time.Duration;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import org.aincraft.database.model.meta.Meta;
import org.aincraft.database.model.meta.Meta.MetaMapping;
import org.aincraft.database.model.test.IMetaStation;
import org.aincraft.database.model.test.IStation;
import org.aincraft.listener.IMetaStationDatabaseService;
import org.aincraft.listener.IStationDatabaseService;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CachedMutableStationDatabaseService<M extends Meta<M>> implements
    IMetaStationDatabaseService<M> {

  private final Cache<UUID, IMetaStation<M>> stationCache = Caffeine.newBuilder().expireAfterWrite(
      Duration.ofMinutes(5)).build();

  private final Cache<Location, IMetaStation<M>> stationLocationCache = Caffeine.newBuilder()
      .expireAfterWrite(Duration.ofMinutes(5)).build();

  private final IStationDatabaseService stationService;

  private final MetaMapping<M> metaMapping;

  public CachedMutableStationDatabaseService(IStationDatabaseService stationService, MetaMapping<M> metaMapping) {
    this.stationService = stationService;
    this.metaMapping = metaMapping;
  }

  @Nullable
  @Override
  public IMetaStation<M> getStation(@NotNull Location location) {
    Preconditions.checkNotNull(location);
    final IMetaStation<M> mutableStation = stationLocationCache.get(location, k -> {
      final IStation station = stationService.getStation(k);
      final M meta = metaMapping.getMeta(station.getIdString());
      return IMetaStation.create(station,meta);
    });
    if(mutableStation == null) {
      return null;
    }
    stationCache.put(mutableStation.getId(), mutableStation);
    return mutableStation;
  }

  @Override
  public @Nullable IMetaStation<M> getStation(@NotNull UUID stationId) {
    Preconditions.checkNotNull(stationId);
    final IMetaStation<M> mutableStation = stationCache.get(stationId, k -> {
      final IStation station = stationService.getStation(k);
      final M meta = metaMapping.getMeta(k.toString());
      return IMetaStation.create(station,meta);
    });
    if (mutableStation == null) {
      return null;
    }
    stationLocationCache.put(mutableStation.getBlockLocation(), mutableStation);
    return mutableStation;
  }

  @Override
  public void removeStation(IStation station) {
    stationService.removeStation(station);
  }

  @Override
  public IMetaStation<M> createStation(Key stationKey, Location location) {
    IStation station = stationService.createStation(stationKey, location);
    final M meta = metaMapping.createMeta(station.getIdString());
    return IMetaStation.create(station,meta);
  }

  @Override
  public void updateStation(IMetaStation<M> mutableStation) {

  }
}
