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

package org.aincraft.inject.implementation.view;

import com.google.common.base.Preconditions;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.papermc.paper.datacomponent.item.ItemLore;
import java.util.List;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
import org.aincraft.Smaug;
import org.aincraft.container.IFactory;
import org.aincraft.container.Result.Status;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.gui.AnvilGuiProxy.UpdatableGuiWrapper;
import org.aincraft.container.gui.ItemFactory;
import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationInventory;
import org.aincraft.database.model.Station.StationMeta;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Creates an updatable recipe selector gui wrapper
 */
public class RecipeSelectorWrapperFactory implements
    IFactory<UpdatableGuiWrapper<SmaugRecipe, PaginatedGui>, Station> {

  private final int rows;
  /**
   *
   */
  private final BiConsumer<InventoryClickEvent, SmaugRecipe> recipeBiConsumer;

  public RecipeSelectorWrapperFactory(int rows, BiConsumer<InventoryClickEvent, SmaugRecipe> recipeBiConsumer) {
    this.rows = rows;
    this.recipeBiConsumer = recipeBiConsumer;
  }

  @Override
  public @NotNull UpdatableGuiWrapper<SmaugRecipe, PaginatedGui> create(@NotNull Station data) {
    Preconditions.checkNotNull(data);
    final StationMeta meta = data.getMeta();
    final StationInventory inventory = meta.getInventory();
    List<SmaugRecipe> recipes = Smaug.fetchAllRecipes(
        r -> r.getStationKey().equals(station.stationKey())
            && r.test(inventory.getContents()).getStatus() == Status.SUCCESS);
    ItemFactory<SmaugRecipe> itemFactory = new ItemFactory.Builder<SmaugRecipe>()
        .setDisplayNameFunction(AbstractGuiWrapperFactory::createRecipeHeader)
        .setItemModelFunction(AbstractGuiWrapperFactory::retrieveItemModel)
        .setLoreFunction(recipe -> {
          final IngredientList ingredientList = recipe.getIngredients();
          @SuppressWarnings("UnstableApiUsage")
          ItemLore lore = ItemLore.lore().addLine(AbstractGuiWrapperFactory.INGREDIENT_TITLE)
              .addLines(ingredientList.components()).build();
          return lore;
        }).build();
    return UpdatableGuiWrapper.create(
        , recipes,
        itemFactory).setClickEventConsumer(recipeBiConsumer).build();
  }
}
