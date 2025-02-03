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
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public interface IStation {

  static IStation create(String idString, String keyString, String worldName, int x, int y, int z) {
    final UUID id = UUID.fromString(idString);
    final Key key = Key.key(keyString);
    final World world = Bukkit.getWorld(worldName);
    return new StationImpl(idString, keyString, worldName, x, y, z, id, key, world,
        new Location(world, x, y, z));
  }

  String getIdString();

  String getKeyString();

  String getWorldName();

  int getBlockX();

  int getBlockY();

  int getBlockZ();

  UUID getId();

  Key getKey();

  World getWorld();

  Location getBlockLocation();

  default Location getCenterLocation() {
    return this.getBlockLocation().clone().add(0.5, 0, 0.5);
  }

  @NotNull
  default BoundingBox getBoundingBox(double offsetX, double offsetZ) {
    final Location location = this.getCenterLocation().clone().add(0,1,0);
    final double x = location.getX();
    final double y = location.getY();
    final double z = location.getZ();
    return new BoundingBox(x + offsetX, y, z + offsetZ,
        x - offsetX, y, z - offsetZ);
  }
}
