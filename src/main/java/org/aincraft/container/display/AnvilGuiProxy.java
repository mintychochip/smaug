package org.aincraft.container.display;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.List;
import net.kyori.adventure.key.Key;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.gui.RecipeGui;

public class AnvilGuiProxy {

  private final Gui gui;

  private final RecipeSelectorItem recipeSelectorItem;

  public AnvilGuiProxy(Gui gui, RecipeSelectorItem recipeSelectorItem) {
    this.gui = gui;
    this.recipeSelectorItem = recipeSelectorItem;
  }

  public Gui getMainGui() {
    return gui;
  }

  public RecipeSelectorItem getRecipeSelectorItem() {
    return recipeSelectorItem;
  }

  public interface AnvilProxyItem {

    GuiItem item();
  }

  public record RecipeSelectorItem(GuiItem item, PaginatedGui recipeSelectorGui,
                                   PaginatedGui allRecipeGui) implements AnvilProxyItem {

    public void update(final Key itemModel, final List<SmaugRecipe> recipes) {
        item.getItemStack()
            .setData(DataComponentTypes.ITEM_MODEL, itemModel);
        recipeSelectorGui.clearPageItems();
        RecipeGui.populateGui(recipeSelectorGui, recipes, null);
      }
    }
}
