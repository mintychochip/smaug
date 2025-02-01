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
import org.aincraft.database.model.MutableStation;
import org.aincraft.database.model.meta.Meta;
import org.aincraft.handler.AbstractStationHandler.ContextImpl;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface StationHandler<M extends Meta<M>> extends Keyed {

  interface Context<M extends Meta<M>> {

    @NotNull
    static <M extends Meta<M>> Context<M> create(@NotNull MutableStation<M> mutableStation,
        @NotNull PlayerInteractEvent event) {
      Preconditions.checkNotNull(mutableStation);
      Preconditions.checkNotNull(event);
      return new ContextImpl(mutableStation, event);
    }

    @NotNull
    MutableStation<M> getStation();

    @NotNull
    PlayerInteractEvent getEvent();

    boolean isRightClick();

    default boolean isLeftClick() {
      return !isRightClick();
    }

    Player getPlayer();

    ItemStack getItem();

    void cancel();

    Block getClickedBlock();
  }

  void handle(Context<M> ctx);
}
