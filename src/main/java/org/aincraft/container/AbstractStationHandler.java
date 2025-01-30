/*
 * MIT License
 *
 * Copyright (c) 2025 mintychochip
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * provided to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.aincraft.container;

import net.kyori.adventure.key.Key;
import org.aincraft.container.StationHandler.Context;
import org.aincraft.database.model.Station;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractStationHandler {
  private final Key stationKey;

  public AbstractStationHandler(Key stationKey) {
    this.stationKey = stationKey;
  }

  public Key getStationKey() {
    return stationKey;
  }

  record ContextImpl(Station station, PlayerInteractEvent event) implements Context {

    @Override
      public @NotNull Station getStation() {
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
    }
}
