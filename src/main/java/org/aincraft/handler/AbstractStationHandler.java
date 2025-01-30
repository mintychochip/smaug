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

package org.aincraft.handler;

import net.kyori.adventure.key.Key;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationMeta;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

abstract class AbstractStationHandler implements StationHandler {
  private final Key key;

  public AbstractStationHandler(Key key) {
    this.key = key;
  }

  @Override
  public @NotNull Key key() {
    return key;
  }

  record ContextImpl<T extends StationMeta>(Station<T> station, PlayerInteractEvent event) implements Context {

    @Override
      public @NotNull Station<T> getStation() {
        return station;
      }

      @Override
      public @NotNull PlayerInteractEvent getEvent() {
        return event;
      }

      @Override
      public boolean isRightClick() {
        final Action a = event.getAction();
        return a.isRightClick();
      }

      @Override
      public Player getPlayer() {
        return event.getPlayer();
      }

      @Override
      public ItemStack getItem() {
        return event.getItem();
      }

      @Override
      public void cancel() {
        event.setCancelled(true);
      }

    @Override
    public Block getClickedBlock() {
      return event.getClickedBlock();
    }
  }
}
