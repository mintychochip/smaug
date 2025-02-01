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

import java.util.UUID;
import net.kyori.adventure.key.Key;
import org.aincraft.database.model.test.IStation;
import org.bukkit.Location;

public interface IStorage {
  IStation getStation(String idString);
  IStation getStation(Location location);
  IStation createStation(String keyString, Location location);
  void removeStation(Location location);

  void close();
}
