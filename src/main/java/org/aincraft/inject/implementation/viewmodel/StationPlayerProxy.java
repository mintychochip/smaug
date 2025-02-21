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

package org.aincraft.inject.implementation.viewmodel;

import org.aincraft.database.model.meta.IMeta;
import org.aincraft.database.model.test.IMetaStation;
import org.bukkit.entity.Player;

public class StationPlayerProxy<M extends IMeta<M>> {

  private final Player player;

  private final IMetaStation<M> station;
  public StationPlayerProxy(Player player, IMetaStation<M> station) {
    this.player = player;
    this.station = station;
  }

  public Player getPlayer() {
    return player;
  }

  public IMetaStation<M> getStation() {
    return station;
  }

  @Override
  public int hashCode() {
    return player.getUniqueId().hashCode() + station.hashCode();
  }
}

