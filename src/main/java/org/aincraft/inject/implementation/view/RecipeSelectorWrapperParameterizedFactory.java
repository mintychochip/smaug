/*
 *
 * Copyright (C) 2025 mintychochip
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.gui.AnvilGuiProxy.UpdatableGuiWrapper;
import org.aincraft.container.gui.ItemParameterizedFactory;
import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.database.model.MutableStation;
import org.aincraft.database.model.meta.TrackableProgressMeta;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Creates an updatable recipe selector gui wrapper
 */
final class RecipeSelectorWrapperParameterizedFactory extends
    AbstractGuiWrapperParameterizedFactory<SmaugRecipe, PaginatedGui, MutableStation<TrackableProgressMeta>> {

  private final BiConsumer<InventoryClickEvent, SmaugRecipe> recipeBiConsumer;

  public RecipeSelectorWrapperParameterizedFactory(int rows, Component title,
      BiConsumer<InventoryClickEvent, SmaugRecipe> recipeBiConsumer) {
    super(rows, title);
    this.recipeBiConsumer = recipeBiConsumer;
  }

  @Override
  public @NotNull UpdatableGuiWrapper<SmaugRecipe, PaginatedGui> create(@NotNull MutableStation<TrackableProgressMeta> data) {
    Preconditions.checkNotNull(data);

    final ItemParameterizedFactory<SmaugRecipe> itemFactory = new ItemParameterizedFactory.Builder<SmaugRecipe>()
        .setDisplayNameFunction(AbstractGuiWrapperParameterizedFactory::createRecipeHeader)
        .setItemModelFunction(AbstractGuiWrapperParameterizedFactory::retrieveItemModel)
        .setLoreFunction(recipe -> {
          final IngredientList ingredientList = recipe.getIngredients();
          @SuppressWarnings("UnstableApiUsage")
          ItemLore lore = ItemLore.lore().addLine(AbstractGuiWrapperParameterizedFactory.INGREDIENT_TITLE)
              .addLines(ingredientList.components()).build();
          return lore;
        }).build();

    final List<SmaugRecipe> recipes = Smaug.fetchAllRecipes(data, null);
    return UpdatableGuiWrapper.create(createGui(), recipes,
        itemFactory).setClickEventConsumer(recipeBiConsumer).build();
  }
}
