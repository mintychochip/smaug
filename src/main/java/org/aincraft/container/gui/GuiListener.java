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

package org.aincraft.container.gui;

import java.util.HashMap;
import java.util.Map;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationGuiAdapter;
import org.aincraft.database.model.Station.StationInventory;
import org.aincraft.database.model.Station.StationMeta;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class GuiListener implements Listener {

  @EventHandler
  private void listenToStationGuiMenuClose(final InventoryCloseEvent event) {
    Inventory inventory = event.getInventory();
    InventoryHolder holder = inventory.getHolder();
    HumanEntity player = event.getPlayer();
    if (!(holder instanceof StationGuiAdapter gui)) {
      return;
    }
    final Station station = gui.getStation();
    StationMeta meta = station.getMeta();
    StationInventory stationInventory = meta.getInventory();
    Map<Integer, ItemStack> inventoryMap = inventoryToMap(inventory);
    Map<Integer, ItemStack> stationInventoryMap = stationInventory.getItems();
    if (inventoryMap.equals(stationInventoryMap)) {
      return;
    }
    station.setMeta(m -> m.setInventory(stationInventory.setItems(inventoryMap)));
    Bukkit.getPluginManager()
        .callEvent(new StationUpdateEvent(station, (Player) player));
  }

  private static Map<Integer, ItemStack> inventoryToMap(Inventory inventory) {
    int size = inventory.getSize();
    Map<Integer, ItemStack> map = new HashMap<>();
    for (int i = 0; i < size; i++) {
      ItemStack item = inventory.getItem(i);
      if (item == null) {
        continue;
      }
      map.put(i, item);
    }
    return map;
  }
}
