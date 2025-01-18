package org.aincraft.container.gui;

import java.util.Map;
import java.util.Map.Entry;
import org.aincraft.database.model.StationInventory;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class StationInventoryGui implements InventoryHolder {

  private final Plugin plugin;
  private final StationInventory stationInventory;

  public StationInventoryGui(Plugin plugin, StationInventory inventory) {
    this.plugin = plugin;
    this.stationInventory = inventory;
  }

  public Plugin getPlugin() {
    return plugin;
  }

  public StationInventory getStationInventory() {
    return stationInventory;
  }

  @Override
  public @NotNull Inventory getInventory() {
    Map<Integer, ItemStack> map = stationInventory.getMap();
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
