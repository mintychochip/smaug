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

package org.aincraft.container.gui;

import java.util.HashMap;
import java.util.Map;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationInventory;
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

//  @EventHandler
//  private void listenToStationGuiMenuClose(final InventoryCloseEvent event) {
//    Inventory inventory = event.getInventory();
//    InventoryHolder holder = inventory.getHolder();
//    HumanEntity player = event.getPlayer();
//    if (!(holder instanceof StationGuiAdapter gui)) {
//      return;
//    }
//    final Station station = gui.getStation();
//    StationMeta meta = station.getMeta();
//    StationInventory stationInventory = meta.getInventory();
//    Map<Integer, ItemStack> inventoryMap = inventoryToMap(inventory);
//    Map<Integer, ItemStack> stationInventoryMap = stationInventory.getItems();
//    if (inventoryMap.equals(stationInventoryMap)) {
//      return;
//    }
//    station.setMeta(m -> m.setInventory(stationInventory.setItems(inventoryMap)));
//    Bukkit.getPluginManager()
//        .callEvent(new StationUpdateEvent(station, (Player) player));
//  }

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
