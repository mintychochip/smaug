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

import com.google.common.base.Preconditions;
import net.kyori.adventure.key.Keyed;
import org.aincraft.database.model.Station;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Station interaction handler keyed by station type. Multiple station types may register handlers.
 */
public interface StationHandler extends Keyed {

  /**
   * Interaction context for a station handler. Plain data — not an interface hierarchy.
   */
  record Context(@NotNull Station station, @NotNull PlayerInteractEvent event) {

    public Context {
      Preconditions.checkNotNull(station);
      Preconditions.checkNotNull(event);
    }

    public static @NotNull Context create(@NotNull Station station,
        @NotNull PlayerInteractEvent event) {
      return new Context(station, event);
    }

    public @NotNull Station getStation() {
      return station;
    }

    public @NotNull PlayerInteractEvent getEvent() {
      return event;
    }

    public boolean isRightClick() {
      final Action a = event.getAction();
      return a.isRightClick();
    }

    public boolean isLeftClick() {
      return !isRightClick();
    }

    public Player getPlayer() {
      return event.getPlayer();
    }

    public ItemStack getItem() {
      return event.getItem();
    }

    public void cancel() {
      event.setCancelled(true);
    }

    public Block getClickedBlock() {
      return event.getClickedBlock();
    }
  }

  void handle(Context ctx);
}
