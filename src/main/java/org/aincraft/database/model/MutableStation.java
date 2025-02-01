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

package org.aincraft.database.model;

import com.google.common.base.Preconditions;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import net.kyori.adventure.key.Key;
import org.aincraft.database.model.meta.Meta;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

/**
 * Model Container, mutable state is held in meta
 *
 * @param idString
 * @param keyString
 * @param worldName
 * @param x
 * @param y
 * @param z
 * @param id
 * @param world
 * @param stationKey
 * @param blockLocation
 * @param metaReference
 * @param <M>
 */
public record MutableStation<M extends Meta<M>>(String idString,
                                                String keyString,
                                                String worldName,
                                                int x, int y, int z, UUID id,
                                                World world, Key stationKey,
                                                Location blockLocation,
                                                AtomicReference<M> metaReference) {

  public static <M extends Meta<M>> MutableStation<M> create(@NotNull String idString,
      @Subst("") @NotNull String keyString, @NotNull String worldName, int x, int y, int z, M meta) {
    Preconditions.checkArgument(
        !(idString == null || keyString == null || worldName == null));
    final World world = Bukkit.getWorld(worldName);
    final Key stationkey = Key.key(keyString);
    if (world == null) {
      return null;
    }
    try {
      UUID id = UUID.fromString(idString);
      return new MutableStation<>(idString, keyString, worldName, x, y, z,
          id, world, stationkey, new Location(world, x, y, z),
          new AtomicReference<>(meta));
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  @NotNull
  public Block getBlock() {
    return world.getBlockAt(blockLocation);
  }

  public MutableStation<M> setMeta(M meta) {
    metaReference.set(meta);
    return this;
  }

  public MutableStation<M> setMeta(Consumer<M> metaConsumer) {
    final M meta = this.getMeta();
    metaConsumer.accept(meta);
    return setMeta(meta);
  }
//  public Station<M> setMeta(Consumer<StationMeta.Builder> metaConsumer) {
//    final StationMeta meta = this.getMeta();
//    Builder builder = new Builder(meta.getRecipeKey(), meta.getProgress(), meta.getInventory());
//    metaConsumer.accept(builder);
//    return setMeta(builder.build());
//  }

  public M getMeta() {
    M meta = metaReference.get();
    return meta.clone();
  }


  @NotNull
  public BoundingBox getBoundingBox(double horizontalOffset) {
    return this.getBoundingBox(horizontalOffset, horizontalOffset);
  }

  @NotNull
  public BoundingBox getBoundingBox(double offsetX, double offsetZ) {
    Location location = blockLocation.clone().add(0.5, 1, 0.5);
    double x = location.getX();
    double y = location.getY();
    double z = location.getZ();
    return new BoundingBox(x + offsetX, y, z + offsetZ,
        x - offsetX, y, z - offsetZ);
  }

  public Location centerLocation() {
    return blockLocation.clone().add(0.5, 0, 0.5);
  }
}
