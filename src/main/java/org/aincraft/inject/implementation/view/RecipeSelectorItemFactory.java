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
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.aincraft.Smaug;
import org.aincraft.container.IFactory;
import org.aincraft.container.Result.Status;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.gui.AnvilGuiProxy.RecipeSelectorItem;
import org.aincraft.container.gui.AnvilGuiProxy.UpdatableGuiItemWrapper;
import org.aincraft.container.gui.AnvilGuiProxy.UpdatableGuiWrapper;
import org.aincraft.container.gui.ItemFactory;
import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.container.item.ItemStackBuilder;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationInventory;
import org.aincraft.database.model.Station.StationMeta;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class RecipeSelectorItemFactory implements
    IFactory<UpdatableGuiWrapper<SmaugRecipe, PaginatedGui>, StationPlayerModelProxy> {

  private static final Component INGREDIENT_TEXT = MiniMessage.miniMessage()
      .deserialize("<italic:false><white>Ingredients:");

  private static final Function<BaseGui, GuiAction<InventoryClickEvent>> GUI_OPEN_INVENTORY_ACTION = gui -> event -> {
    HumanEntity entity = event.getWhoClicked();
    gui.open(entity);
  };

  private static Component recipeItemHeader(@NotNull SmaugRecipe recipe) {
    Preconditions.checkNotNull(recipe);
    Component displayName = RecipeSelectorItemFactory.retrieveDisplayName(recipe);
    return MiniMessage.miniMessage()
        .deserialize("Recipe: <a>", Placeholder.component("a", displayName));
  }

  private static UpdatableGuiWrapper<SmaugRecipe, PaginatedGui> createRecipeSelectorGuiWrapper(
      Station station,
      BiConsumer<InventoryClickEvent, SmaugRecipe> recipeBiConsumer) {
    StationMeta meta = station.getMeta();
    StationInventory inventory = meta.getInventory();
    List<SmaugRecipe> recipes = Smaug.fetchAllRecipes(
        r -> r.getStationKey().equals(station.stationKey())
            && r.test(inventory.getContents()).getStatus() == Status.SUCCESS);
    ItemFactory<SmaugRecipe> itemFactory = new ItemFactory.Builder<SmaugRecipe>()
        .setItemModelFunction(RecipeSelectorItemFactory::retrieveItemModel)
        .setDisplayNameFunction(RecipeSelectorItemFactory::recipeItemHeader)
        .setLoreFunction(recipe -> {
          final IngredientList ingredientList = recipe.getIngredients();
          @SuppressWarnings("UnstableApiUsage")
          ItemLore lore = ItemLore.lore().addLine(INGREDIENT_TEXT)
              .addLines(ingredientList.components()).build();
          return lore;
        }).build();
    return UpdatableGuiWrapper.create(
        createGui(Component.text("Recipes")), recipes,
        itemFactory).setClickEventConsumer(recipeBiConsumer).build();
  }

  private static GuiItem guiReferenceItem(ItemStack stack, BaseGui gui) {
    return new GuiItem(stack, e -> {
      HumanEntity entity = e.getWhoClicked();
      gui.open(entity);
    });
  }

  private static GuiItem createFillerItem(BaseGui gui) {
    ItemStack stack = ItemStackBuilder.create(Material.RABBIT_FOOT).meta(meta -> meta
        .itemModel(Material.GRAY_STAINED_GLASS_PANE)
        .displayName(Component.empty())).build();
    return guiReferenceItem(stack, gui);
  }

  private static void addStaticItems(PaginatedGui gui, BaseGui mainGui) {
    final GuiItem filler = createFillerItem(mainGui);
    final int rows = gui.getRows();
    ItemStack stack = ItemStackBuilder.create(Material.RABBIT_FOOT)
        .meta(meta -> meta.itemModel(Material.PAPER)).build();
    gui.setItem(rows, 1,
        ItemStackBuilder.create(stack).meta(meta -> meta.displayName(Component.text("Previous")))
            .asGuiItem(e -> gui.previous()));
    gui.setItem(rows, 9,
        ItemStackBuilder.create(stack).meta(meta -> meta.displayName(Component.text("Next")))
            .asGuiItem(e -> gui.next()));
    gui.getFiller().fillBetweenPoints(rows, 3, rows, 8, filler);
  }

  private static void createStaticItemsAndLink(UpdatableGuiWrapper<?, PaginatedGui> guiWrapper,
      BaseGui mainGui,
      GuiItem item) {
    PaginatedGui gui = guiWrapper.getGui();
    final int rows = gui.getRows();
    addStaticItems(gui, mainGui);
    gui.setItem(rows, 2, item);
  }

  private static RecipeSelectorItem create(Station station, Player player, BaseGui mainGui,
      BiConsumer<InventoryClickEvent, SmaugRecipe> recipeSelectorConsumer) {
    UpdatableGuiWrapper<SmaugRecipe, PaginatedGui> codexGuiWrapper = new CodexGuiWrapperFactory(
        AnvilGuiProxyFactory.ROWS, Component.text("Codex")).create(station);
    UpdatableGuiWrapper<SmaugRecipe, PaginatedGui> recipeSelectorGuiWrapper = createRecipeSelectorGuiWrapper(
        station,
        recipeSelectorConsumer);

    GuiItem linkedRecipeItem = ItemStackBuilder.create(Material.RABBIT_FOOT)
        .meta(meta -> meta.itemModel(Material.BOOK)
            .displayName(Component.text("Recipes")))
        .asGuiItem(GUI_OPEN_INVENTORY_ACTION.apply(recipeSelectorGuiWrapper.getGui()));

    GuiItem linkedCodexItem = ItemStackBuilder.create(Material.RABBIT_FOOT)
        .meta(meta -> meta.itemModel(Material.WRITABLE_BOOK)
            .displayName(Component.text("Codex")))
        .asGuiItem(GUI_OPEN_INVENTORY_ACTION.apply(codexGuiWrapper.getGui()));

    createStaticItemsAndLink(recipeSelectorGuiWrapper, mainGui, linkedCodexItem);
    createStaticItemsAndLink(codexGuiWrapper, mainGui, linkedRecipeItem);

    codexGuiWrapper.setUpdateConsumer(w -> {
      w.getGui().clearPageItems();
      createStaticItemsAndLink(w, mainGui, linkedRecipeItem);
    });
    recipeSelectorGuiWrapper.setUpdateConsumer(w -> {
      w.getGui().clearPageItems();
      createStaticItemsAndLink(w, mainGui, linkedCodexItem);
    });

    final StationMeta meta = station.getMeta();
    final String recipeKey = meta.getRecipeKey();
    return new RecipeSelectorItem(
        UpdatableGuiItemWrapper.create(recipeKey != null ? Smaug.fetchRecipe(recipeKey) : null,
            new ItemFactory.Builder<SmaugRecipe>().setDisplayNameFunction(r -> {
                  if (r == null) {
                    return Component.text("No Recipe Selected");
                  }
                  return MiniMessage.miniMessage().deserialize("Selected: <a>",
                      Placeholder.component("a", retrieveDisplayName(r)));
                })
                .setItemModelFunction(RecipeSelectorItemFactory::retrieveItemModel).build(),
            e -> {
              if (e.isLeftClick()) {
                recipeSelectorGuiWrapper.open(player);
              } else {
                codexGuiWrapper.open(player);
              }
            }),
        recipeSelectorGuiWrapper, codexGuiWrapper);
  }

  @Override
  public @NotNull UpdatableGuiWrapper<SmaugRecipe, PaginatedGui> create(@NotNull StationPlayerModelProxy data) {
    return null;
  }
}
