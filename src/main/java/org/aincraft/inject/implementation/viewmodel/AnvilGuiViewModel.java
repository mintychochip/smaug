package org.aincraft.inject.implementation.viewmodel;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import java.util.List;

import org.aincraft.Smaug;
import org.aincraft.container.IFactory;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.gui.AnvilGuiProxy;
import org.aincraft.container.gui.AnvilGuiProxy.RecipeSelectorItem;
import org.aincraft.database.model.Station.StationMeta;
import org.aincraft.inject.implementation.view.AnvilGuiProxyFactory;
import org.aincraft.inject.implementation.view.AnvilGuiProxyFactory.RecipeSelectorItemFactory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public final class AnvilGuiViewModel extends
    AbstractViewModel<StationPlayerModelProxy, AnvilGuiProxy, Integer> {

  private final AnvilGuiProxyFactory factory;

  AnvilGuiViewModel(AnvilGuiProxyFactory factory) {
    this.factory = factory;
  }

  static final class AnvilGuiBinding extends AbstractBinding {

    @ExposedProperty("gui")
    private final Gui mainGui;

    private final RecipeSelectorItem recipeSelectorItem;

    AnvilGuiBinding(Gui mainGui, RecipeSelectorItem recipeSelectorItem) {
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
  public void update(@NotNull StationPlayerModelProxy model) {
    final AnvilGuiBinding binding = (AnvilGuiBinding) this.getBinding(model);

    final RecipeSelectorItem recipeSelectorItem = binding.recipeSelectorItem();
    final Gui mainGui = binding.mainGui();
    final Player player = model.player();
    StationMeta meta = model.station().getMeta();
    String recipeKey = meta.getRecipeKey();
    recipeSelectorItem.updateIcon(Smaug.fetchRecipe(recipeKey));
    List<SmaugRecipe> recipes = RecipeSelectorItemFactory.retrieveAllAvailableRecipes(meta);
    recipeSelectorItem.recipeSelectorGui().update(recipes);
    recipeSelectorItem.codexGui().update(Smaug.fetchAllRecipes(model.station().stationKey()));
    for (BaseGui gui : List.of(mainGui, recipeSelectorItem.recipeSelectorGui().getGui(),
        recipeSelectorItem.codexGui().getGui())) {
      playerIsViewingUpdate(player, gui);
    }
  }

  @Override
  @NotNull
  Class<? extends IViewModelBinding> getBindingClass() {
    return AnvilGuiBinding.class;
  }

  @Override
  @NotNull
  IFactory<AnvilGuiProxy, StationPlayerModelProxy> getViewFactory() {
    return factory;
  }

  @Override
  @NotNull
  IViewModelBinding viewToBinding(AnvilGuiProxy view) {
    return new AnvilGuiBinding(view.getMainGui(), view.getRecipeSelectorItem());
  }

  @Override
  @NotNull Integer modelToKey(@NotNull StationPlayerModelProxy model) {
    return model.hashCode();
  }

  private static void playerIsViewingUpdate(HumanEntity entity, BaseGui gui) {
    final Inventory inventory = gui.getInventory();
    final List<HumanEntity> viewers = inventory.getViewers();
    if (viewers.contains(entity)) {
      gui.update();
    }
  }
}
