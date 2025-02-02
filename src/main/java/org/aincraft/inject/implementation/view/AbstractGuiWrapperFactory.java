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

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.aincraft.container.IFactory;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.gui.AnvilGuiProxy.UpdatableGuiWrapper;
import org.aincraft.util.Util;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AbstractGuiWrapperFactory<T, G extends BaseGui, D> implements
    IFactory<UpdatableGuiWrapper<T, G>, D> {

  protected static final Component INGREDIENT_TITLE;

  static {
    INGREDIENT_TITLE = MiniMessage.miniMessage().deserialize("<italic:false><white>Ingredients");
  }

  protected final int rows;

  protected final Component title;

  AbstractGuiWrapperFactory(int rows, Component title) {
    this.rows = rows;
    this.title = title;
  }

  public Component getTitle() {
    return title;
  }

  public int getRows() {
    return rows;
  }

  protected int pageSize() {
    return 9 * (rows - 1);
  }

  protected PaginatedGui createGui() {
    return Gui.paginated().title(this.title).rows(this.rows).pageSize(this.pageSize())
        .disableAllInteractions().create();
  }

  static Component createRecipeHeader(SmaugRecipe recipe) {
    final Component displayName = Util.retrieveDisplayName(recipe.getOutput().getReference());
    return MiniMessage.miniMessage()
        .deserialize("<italic:false><white>Recipe: <a>", Placeholder.component("a", displayName));
  }

  @NotNull
  static Key retrieveItemModel(@Nullable SmaugRecipe recipe) {
    if (recipe == null) {
      return Material.MAP.getKey();
    }
    return Util.retrieveItemModel(recipe.getOutput().getReference());
  }
}
