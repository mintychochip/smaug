package org.aincraft.container.gui;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import java.util.List;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
import org.aincraft.container.SmaugRecipe;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RecipeGuiFactory {

  private final RecipeGuiItemFactory itemFactory;

  private static final int ROWS = 4;
  private static final int PAGE_SIZE = 9 * (ROWS - 1);

  public RecipeGuiFactory(RecipeGuiItemFactory itemFactory) {
    this.itemFactory = itemFactory;
  }

  public PaginatedGui create(List<SmaugRecipe> recipes, Component title,
      BiConsumer<InventoryClickEvent, SmaugRecipe> recipeBiConsumer) {
    PaginatedGui gui = Gui.paginated().disableAllInteractions().title(title).rows(ROWS)
        .pageSize(PAGE_SIZE).create();
    for (SmaugRecipe recipe : recipes) {
      GuiItem guiItem = itemFactory.create(recipe);
      assert guiItem != null;
      if (recipeBiConsumer != null) {
        guiItem.setAction(e -> {
          recipeBiConsumer.accept(e, recipe);
        });
      }
      gui.addItem(guiItem);
    }
    return gui;
  }
}
