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

package org.aincraft.api.event;

import org.aincraft.database.model.meta.Meta;
import org.aincraft.database.model.test.IMetaStation;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class StationUpdateEvent<M extends Meta<M>> extends Event implements Cancellable {

  private static HandlerList handlers = new HandlerList();

  private final IMetaStation<M> metaStation;

  private final Player player;

  private boolean cancelled = false;

  StationUpdateEvent(IMetaStation<M> metaStation, Player player) {
    this.metaStation = metaStation;
    this.player = player;
  }


  public Player getViewer() {
    return player;
  }

  public IMetaStation<M> getStation() {
    return metaStation;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancel) {
    this.cancelled = cancel;
  }
}
