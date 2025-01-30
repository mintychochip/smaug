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

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.aincraft.container.IFactory;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.gui.AnvilGuiProxy.UpdatableGuiWrapper;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.util.Util;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
