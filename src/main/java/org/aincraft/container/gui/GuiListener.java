package org.aincraft.container.gui;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import org.aincraft.api.event.StationInventoryEvent;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.database.model.StationInventory;
import org.aincraft.listener.IStationService;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

public class GuiListener implements Listener {

  private final Plugin plugin;
  private final IStationService stationService;

  @Inject
  public GuiListener(Plugin plugin, IStationService stationService) {
    this.plugin = plugin;
    this.stationService = stationService;
  }

  @EventHandler
  private void listenToStationGuiMenuClose(final InventoryCloseEvent event) {
    Inventory inventory = event.getInventory();
    InventoryHolder holder = inventory.getHolder();
    if (!(holder instanceof StationInventoryGui gui)) {
      return;
    }
    if (!(plugin.equals(gui.getPlugin()))) {
      return;
    }
    final StationInventory stationInventory = gui.getStationInventory();
    if (stationInventory == null) {
      return;
    }
    Map<Integer, ItemStack> inventoryMap = inventoryToMap(inventory);
    Map<Integer, ItemStack> stationInventoryMap = stationInventory.getMap();
    if(inventoryMap.equals(stationInventoryMap)) {
      return;
    }
    Bukkit.getPluginManager()
        .callEvent(new StationInventoryEvent(stationInventory.setItems(inventoryMap)));
  }

  @EventHandler
  private void listenToRecipeMenu(final InventoryClickEvent event) {
    Inventory clicked = event.getClickedInventory();
    HumanEntity entity = event.getWhoClicked();
    InventoryView view = entity.getOpenInventory();
    Inventory open = view.getTopInventory();
    if (!(open.getHolder() instanceof RecipeMenu menu)) {
      return;
    }
    PlayerInventory inventory = entity.getInventory();
    if (inventory.equals(clicked)) {
      event.setCancelled(true);
      return;
    }
    event.setCancelled(true);
    if (event.getClick() == ClickType.RIGHT) {
      SmaugRecipe recipe = menu.getRecipe(event.getSlot());
      if (recipe == null) {
        return;
      }
      entity.closeInventory(Reason.PLUGIN);
      menu.getRecipeConsumer().accept(recipe);
    }
  }

  private static Map<Integer,ItemStack> inventoryToMap(Inventory inventory) {
    int size = inventory.getSize();
    Map<Integer,ItemStack> map = new HashMap<>();
    for(int i = 0; i < size; i++) {
      ItemStack item = inventory.getItem(i);
      if(item == null) {
        continue;
      }
      map.put(i,item);
    }
    return map;
  }
}
