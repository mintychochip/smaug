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

class StationImpl implements IStation {

  private final String idString;
  private final String keyString;
  private final String worldName;
  private final int x;
  private final int y;
  private final int z;
  private final UUID id;
  private final Key key;
  private final World world;
  private final Location blockLocation;

  StationImpl(String idString, String keyString, String worldName, int x, int y, int z, UUID id,
      Key key, World world, Location blockLocation) {
    this.idString = idString;
    this.keyString = keyString;
    this.worldName = worldName;
    this.x = x;
    this.y = y;
    this.z = z;
    this.id = id;
    this.key = key;
    this.world = world;
    this.blockLocation = blockLocation;
  }

  @Override
  public String getIdString() {
    return idString;
  }

  @Override
  public String getKeyString() {
    return keyString;
  }

  @Override
  public String getWorldName() {
    return worldName;
  }

  @Override
  public int getBlockX() {
    return x;
  }

  @Override
  public int getBlockY() {
    return y;
  }

  @Override
  public int getBlockZ() {
    return z;
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public Key getKey() {
    return key;
  }

  @Override
  public World getWorld() {
    return world;
  }

  @Override
  public Location getBlockLocation() {
    return blockLocation;
  }
}
