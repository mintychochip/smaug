package org.aincraft.container.gui;

import java.util.HashMap;
import java.util.Map;
import org.aincraft.Smaug;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.database.model.Station;
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
    if (!(holder instanceof StationInventoryGui gui)) {
      return;
    }
    if (!(Smaug.getPlugin().equals(gui.getPlugin()))) {
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
    meta.setInventory(stationInventory.setItems(inventoryMap));
    station.setMeta(meta);
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
