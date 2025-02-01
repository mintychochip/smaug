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
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.aincraft.Smaug;
import org.aincraft.container.IParameterizedFactory;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.gui.AnvilGuiProxy.RecipeSelectorItem;
import org.aincraft.container.gui.AnvilGuiProxy.UpdatableGuiItemWrapper;
import org.aincraft.container.gui.AnvilGuiProxy.UpdatableGuiWrapper;
import org.aincraft.container.gui.ItemParameterizedFactory;
import org.aincraft.container.item.ItemStackBuilder;
import org.aincraft.database.model.MutableStation;
import org.aincraft.database.model.meta.TrackableProgressMeta;
import org.aincraft.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

final class RecipeSelectorItemParameterizedFactory implements
    IParameterizedFactory<RecipeSelectorItem, MutableStation<TrackableProgressMeta>> {

  private final Player player;
  private final BaseGui mainGui;
  private final IParameterizedFactory<UpdatableGuiWrapper<SmaugRecipe, PaginatedGui>, MutableStation<TrackableProgressMeta>> codexGuiParameterizedFactory;
  private final IParameterizedFactory<UpdatableGuiWrapper<SmaugRecipe, PaginatedGui>, MutableStation<TrackableProgressMeta>> recipeSelectorGuiParameterizedFactory;
  private final GuiItem filler;

  RecipeSelectorItemParameterizedFactory(Player player, BaseGui mainGui,
      IParameterizedFactory<UpdatableGuiWrapper<SmaugRecipe, PaginatedGui>, MutableStation<TrackableProgressMeta>> codexGuiParameterizedFactory,
      IParameterizedFactory<UpdatableGuiWrapper<SmaugRecipe, PaginatedGui>, MutableStation<TrackableProgressMeta>> recipeSelectorGuiParameterizedFactory,
      GuiItem filler) {
    this.player = player;
    this.mainGui = mainGui;
    this.codexGuiParameterizedFactory = codexGuiParameterizedFactory;
    this.recipeSelectorGuiParameterizedFactory = recipeSelectorGuiParameterizedFactory;
    this.filler = filler;
  }

  private static void createStaticItemsAndLink(PaginatedGui gui,
      GuiItem item) {
    int rows = gui.getRows();
    ItemStackBuilder stackBuilder = ItemStackBuilder.create(Material.RABBIT_FOOT)
        .meta(m -> m.itemModel(Material.PAPER));
    Map<Integer, GuiItem> items = new HashMap<>();
    items.put(1, stackBuilder.meta(m -> m.displayName(Component.text("Previous")))
        .asGuiItem(e -> gui.previous()));
    items.put(9,
        stackBuilder.meta(m -> m.displayName(Component.text("Next"))).asGuiItem(e -> gui.next()));
    items.put(2, item);
    items.forEach((slot, i) -> {
      gui.setItem(rows, slot, i);
    });
  }

  private static GuiItem createLinkedWrapperGuiItem(Material material, Component displayName,
      BaseGui linkedGui) {
    return ItemStackBuilder.create(Material.RABBIT_FOOT)
        .meta(m -> m.itemModel(material).displayName(displayName)).asGuiItem(e -> {
          HumanEntity entity = e.getWhoClicked();
          linkedGui.open(entity);
        });
  }

  @Override
  public @NotNull RecipeSelectorItem create(@NotNull MutableStation<TrackableProgressMeta> data) {
    final UpdatableGuiWrapper<SmaugRecipe, PaginatedGui> codexGuiWrapper = codexGuiParameterizedFactory.create(
        data);
    final UpdatableGuiWrapper<SmaugRecipe, PaginatedGui> recipeSelectorGuiWrapper = recipeSelectorGuiParameterizedFactory.create(
        data);

    final GuiItem linkedRecipeItem = createLinkedWrapperGuiItem(Material.BOOK,
        Component.text("Recipes"),
        recipeSelectorGuiWrapper.getGui());

    final GuiItem linkedCodexItem = createLinkedWrapperGuiItem(Material.WRITABLE_BOOK,
        Component.text("Codex"), codexGuiWrapper.getGui());

    createStaticItemsAndLink(recipeSelectorGuiWrapper.getGui(), linkedCodexItem);
    createStaticItemsAndLink(codexGuiWrapper.getGui(), linkedRecipeItem);

    codexGuiWrapper.setUpdateConsumer(w -> {
      w.getGui().clearPageItems();
      createStaticItemsAndLink(w.getGui(), linkedRecipeItem);
    });
    recipeSelectorGuiWrapper.setUpdateConsumer(w -> {
      w.getGui().clearPageItems();
      createStaticItemsAndLink(w.getGui(), linkedCodexItem);
    });

    final TrackableProgressMeta meta = data.getMeta();
    final String recipeKey = meta.getRecipeKey();
    return new RecipeSelectorItem(
        UpdatableGuiItemWrapper.create(recipeKey != null ? Smaug.fetchRecipe(recipeKey) : null,
            new ItemParameterizedFactory.Builder<SmaugRecipe>().setDisplayNameFunction(r -> {
                  if (r == null) {
                    return Component.text("No Recipe Selected");
                  }
                  return MiniMessage.miniMessage().deserialize("Selected: <a>",
                      Placeholder.component("a",
                          Util.retrieveDisplayName(r.getOutput().getReference())));
                })
                .setItemModelFunction(AbstractGuiWrapperParameterizedFactory::retrieveItemModel).build(),
            e -> {
              if (e.isLeftClick()) {
                recipeSelectorGuiWrapper.open(player);
              } else {
                codexGuiWrapper.open(player);
              }
            }),
        recipeSelectorGuiWrapper, codexGuiWrapper);
  }
}
