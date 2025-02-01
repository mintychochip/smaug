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

import java.util.UUID;
import net.kyori.adventure.key.Key;
import org.aincraft.database.model.MutableStation;
import org.aincraft.database.model.meta.Meta;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IMutableStationService<M extends Meta<M>> {

  MutableStation<M> createStation(Key stationKey, Location location);

  void updateStation(MutableStation<M> mutableStation);

  void deleteStation(Location location);

  @Nullable
  MutableStation<M> getStation(@NotNull Location location);

  @Nullable
  MutableStation<M> getStation(@NotNull UUID stationId);
}
