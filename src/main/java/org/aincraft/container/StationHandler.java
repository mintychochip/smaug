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

package org.aincraft.container;

import com.google.common.base.Preconditions;
import org.aincraft.container.AbstractStationHandler.ContextImpl;
import org.aincraft.database.model.Station;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface StationHandler {

  interface Context {

    @NotNull
    static Context create(@NotNull Station station, @NotNull PlayerInteractEvent event) {
      Preconditions.checkNotNull(station);
      Preconditions.checkNotNull(event);
      return new ContextImpl(station, event);
    }

    @NotNull
    Station getStation();

    @NotNull
    PlayerInteractEvent getEvent();

    boolean isRightClick();

    default boolean isLeftClick() {
      return !isRightClick();
    }

    Player getPlayer();

    ItemStack getItem();

    void cancel();
  }

  void handle(Context ctx);
}
