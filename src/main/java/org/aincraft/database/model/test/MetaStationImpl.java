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

package org.aincraft.database.model.test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import net.kyori.adventure.key.Key;
import org.aincraft.database.model.meta.Meta;
import org.bukkit.Location;
import org.bukkit.World;

final class MetaStationImpl<M extends Meta<M>> extends StationImpl implements IMetaStation<M> {

  private final AtomicReference<M> metaReference;

  MetaStationImpl(String idString, String keyString, String worldName, int x, int y, int z, UUID id,
      Key key, World world, Location blockLocation, M meta) {
    super(idString, keyString, worldName, x, y, z, id, key, world, blockLocation);
    this.metaReference = new AtomicReference<>(meta);
  }

  static <M extends Meta<M>> MetaStationImpl<M> create(IStation station, M meta) {
    return new MetaStationImpl<>(station.getIdString(), station.getKeyString(),
        station.getWorldName(), station.getBlockX(), station.getBlockY(), station.getBlockZ(),
        station.getId(), station.getKey(), station.getWorld(), station.getBlockLocation(), meta);
  }

  @Override
  public IMetaStation<M> setMeta(M meta) {
    metaReference.set(meta);
    return this;
  }

  @Override
  public IMetaStation<M> setMeta(Consumer<M> metaConsumer) {
    final M meta = this.getMeta();
    metaConsumer.accept(meta);
    return this.setMeta(meta);
  }

  @Override
  public IMetaStation<M> setMeta(Function<M,M> metaFunction) {
    final M meta = this.getMeta();
    final M appliedMeta = metaFunction.apply(meta);
    return this.setMeta(appliedMeta);
  }

  @Override
  public M getMeta() {
    final M meta = metaReference.get();
    return meta.clone();
  }

  @Override
  public String toString() {
    return "MetaStationImpl{" +
        "metaReference=" + metaReference +
        '}';
  }
}
