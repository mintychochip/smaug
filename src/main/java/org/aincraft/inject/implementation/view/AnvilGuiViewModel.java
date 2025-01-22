package org.aincraft.inject.implementation.view;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aincraft.Smaug;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.display.AnvilGuiProxy;
import org.aincraft.container.display.AnvilGuiProxy.RecipeSelectorItem;
import org.aincraft.container.display.Binding;
import org.aincraft.container.display.IViewModel;
import org.aincraft.database.model.Station.StationMeta;
import org.aincraft.inject.implementation.controller.AbstractBinding;
import org.aincraft.inject.implementation.view.AnvilGuiProxyFactory.RecipeSelectorItemFactory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class AnvilGuiViewModel implements IViewModel<StationPlayerModelProxy, AnvilGuiProxy> {

  private final Map<Integer, ViewBinding> bindings = new HashMap<>();

  static final class ViewBinding extends AbstractBinding {

    @ExposedProperty("gui")
    private final Gui mainGui;

    private final RecipeSelectorItem recipeSelectorItem;

    ViewBinding(Gui mainGui, RecipeSelectorItem recipeSelectorItem) {
      this.mainGui = mainGui;
      this.recipeSelectorItem = recipeSelectorItem;
    }

    public Gui mainGui() {
      return mainGui;
    }

    public RecipeSelectorItem recipeSelectorItem() {
      return recipeSelectorItem;
    }
  }

  @Override
  public void bind(@NotNull StationPlayerModelProxy model, @NotNull AnvilGuiProxy view) {
    bindings.put(model.hashCode(),
        new ViewBinding(view.getMainGui(), view.getRecipeSelectorItem()));
  }

  @Override
  public void update(@NotNull StationPlayerModelProxy model, @NotNull Object... data) {
    final ViewBinding binding = bindings.get(model.hashCode());
    final RecipeSelectorItem recipeSelectorItem = binding.recipeSelectorItem();
    final Gui mainGui = binding.mainGui();
    final Player player = model.player();
    StationMeta meta = model.station().getMeta();
    SmaugRecipe recipe = Smaug.fetchRecipe(meta.getRecipeKey());
    recipeSelectorItem.update(
        RecipeSelectorItemFactory.retrieveItemModel(recipe),
        RecipeSelectorItemFactory.retrieveAllAvailableRecipes(model.station().getMeta(),
            model.player()));
    if (playerIsViewing(player, mainGui)) {
      mainGui.update();
    }
    if (playerIsViewing(player, recipeSelectorItem.recipeSelectorGui())) {
      recipeSelectorItem.recipeSelectorGui().update();
    }
  }

  private static boolean playerIsViewing(HumanEntity humanEntity, BaseGui gui) {
    final Inventory inventory = gui.getInventory();
    final List<HumanEntity> viewers = inventory.getViewers();
    return viewers.contains(humanEntity);
  }

  @Override
  public void remove(@NotNull Object modelKey) {
    bindings.remove((Integer) modelKey);
  }

  @Override
  public void removeAll() {
    bindings.clear();
  }

  @Override
  public boolean isBound(@NotNull Object modelKey) {
    return bindings.containsKey((Integer) modelKey);
  }

  @Override
  public Binding getBinding(StationPlayerModelProxy model) {
    return bindings.get(model.hashCode());
  }
}
