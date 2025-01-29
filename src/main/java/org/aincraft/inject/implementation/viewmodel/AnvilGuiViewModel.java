/*
 * MIT License
 *
 * Copyright (c) 2025 mintychochip
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * provided to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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

    @ExposedProperty("recipe-selector")
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
    final String recipeKey = meta.getRecipeKey();
    recipeSelectorItem.itemWrapper().update(recipeKey != null ? Smaug.fetchRecipe(recipeKey) : null);
    List<SmaugRecipe> recipes = Smaug.getRecipeFetcher()
        .all(model.station().stationKey(), meta.getInventory()
            .getContents());
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
  IViewModelBinding viewToBinding(@NotNull AnvilGuiProxy view) {
    return new AnvilGuiBinding(view.getMainGui(), view.getRecipeSelectorItem());
  }

  @Override
  @NotNull
  Integer modelToKey(@NotNull StationPlayerModelProxy model) {
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
