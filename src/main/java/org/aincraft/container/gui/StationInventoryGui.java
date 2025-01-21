package org.aincraft.container.gui;

import java.util.Map;
import java.util.Map.Entry;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationInventory;
import org.aincraft.database.model.Station.StationMeta;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class StationInventoryGui implements InventoryHolder {

  private final Plugin plugin;
  private final Station station;
  public StationInventoryGui(Plugin plugin, Station station) {
    this.plugin = plugin;
    this.station = station;
  }

  public Station getStation() {
    return station;
  }

  public Plugin getPlugin() {
    return plugin;
  }

  @Override
  public @NotNull Inventory getInventory() {
    StationMeta meta = station.getMeta();
    StationInventory stationInventory = meta.getInventory();
    Map<Integer, ItemStack> map = stationInventory.getItems();
    int i = inventorySize(map.size());
    Inventory inventory = Bukkit.createInventory(this, i);
    for (Entry<Integer, ItemStack> entry : map.entrySet()) {
      inventory.setItem(entry.getKey(),entry.getValue());
    }
    return inventory;
  }

  static int inventorySize(int size) {
    if (size <= 9) {
      return 9;
    }

    if (size > 54) {
      return 54;
    }

    return (int) Math.ceil(size / 9.0) * 9;
  }
}
