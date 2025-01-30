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
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.papermc.paper.datacomponent.item.ItemLore;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.aincraft.Smaug;
import org.aincraft.container.IFactory;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.gui.AnvilGuiProxy;
import org.aincraft.container.gui.AnvilGuiProxy.UpdatableGuiWrapper;
import org.aincraft.container.gui.ItemFactory;
import org.aincraft.container.gui.ItemFactory.Builder;
import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.database.model.Station;
import org.jetbrains.annotations.NotNull;

/**
 * Creates an updatable codex gui wrapper
 */
final class CodexGuiWrapperFactory extends
    AbstractGuiWrapperFactory<SmaugRecipe, PaginatedGui, Station> {

  CodexGuiWrapperFactory(int rows, Component title) {
    super(rows, title);
  }

  @SuppressWarnings("UnstableApiUsage")
  @Override
  public @NotNull AnvilGuiProxy.UpdatableGuiWrapper<SmaugRecipe, PaginatedGui> create(
      @NotNull Station data) {
    Preconditions.checkNotNull(data);
    final ItemFactory<SmaugRecipe> itemFactory = new Builder<SmaugRecipe>().setDisplayNameFunction(
            AbstractGuiWrapperFactory::createRecipeHeader)
        .setItemModelFunction(AbstractGuiWrapperFactory::retrieveItemModel)
        .setLoreFunction(recipe -> {
          final IngredientList ingredientList = recipe.getIngredients();
          ItemLore.Builder builder = ItemLore.lore()
              .addLine(AbstractGuiWrapperFactory.INGREDIENT_TITLE)
              .addLines(ingredientList.components());
          IngredientList missing = ingredientList.findMissing(
              data.getMeta().getInventory().getContents());
          if (!missing.isEmpty()) {
            builder.addLine(MiniMessage.miniMessage()
                    .deserialize("<italic:false><white>Missing Ingredients:"))
                .addLines(missing.components());
          }
          return builder.build();
        }).build();
    final List<SmaugRecipe> recipes = Smaug.fetchAllRecipes(data);
    return UpdatableGuiWrapper.create(createGui(), recipes, itemFactory).build();
  }
}
