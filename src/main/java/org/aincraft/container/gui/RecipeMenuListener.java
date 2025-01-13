package org.aincraft.container.gui;

import com.google.inject.Inject;
import org.aincraft.container.SmaugRecipe;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

public class RecipeMenuListener implements Listener {

  private final Plugin plugin;

  @Inject
  public RecipeMenuListener(Plugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  private void listenToRecipeMenu(final InventoryClickEvent event) {
    Inventory clicked = event.getClickedInventory();
    HumanEntity entity = event.getWhoClicked();
    InventoryView view = entity.getOpenInventory();
    Inventory open = view.getTopInventory();
    if(!(open.getHolder() instanceof RecipeMenu menu)) {
      return;
    }
    PlayerInventory inventory = entity.getInventory();
    if(inventory.equals(clicked)) {
      event.setCancelled(true);
      return;
    }
    if(!(plugin.equals(menu.getPlugin()))) {
      return;
    }
    event.setCancelled(true);
    if(event.getClick() == ClickType.RIGHT) {
      SmaugRecipe recipe = menu.getRecipe(event.getSlot());
      if(recipe == null) {
        return;
      }
      entity.closeInventory(Reason.PLUGIN);
      menu.getRecipeConsumer().accept(recipe);
    }
  }
}
