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
import dev.triumphteam.gui.components.GuiType;
import dev.triumphteam.gui.components.util.GuiFiller;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
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
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.IFactory;
import org.aincraft.container.Result.Status;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.gui.AnvilGuiProxy;
import org.aincraft.container.gui.AnvilGuiProxy.UpdatableGuiItemWrapper;
import org.aincraft.container.gui.AnvilGuiProxy.GuiWrapper;
import org.aincraft.container.gui.AnvilGuiProxy.MetaItem;
import org.aincraft.container.gui.AnvilGuiProxy.RecipeSelectorItem;
import org.aincraft.container.gui.AnvilGuiProxy.BasicStationItem;
import org.aincraft.container.gui.ItemFactory.Builder;
import org.aincraft.container.item.ItemStackBuilder;
import org.aincraft.container.gui.ItemFactory;
import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationInventory;
import org.aincraft.database.model.Station.StationMeta;
import org.aincraft.listener.IStationService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnvilGuiProxyFactory implements IFactory<AnvilGuiProxy, StationPlayerModelProxy> {

  private static final GuiType GUI_TYPE = GuiType.DISPENSER;
  private static final Component MAIN_GUI_TITLE = Component.text("Menu");

  private final IStationService stationService;
  private final Plugin plugin;

  public AnvilGuiProxyFactory(IStationService stationService, Plugin plugin) {
    this.stationService = stationService;
    this.plugin = plugin;
  }

  @Override
  public @NotNull AnvilGuiProxy create(StationPlayerModelProxy data) {
    Station station = data.station();
    Player player = data.player();
    Gui main = Gui.gui(GUI_TYPE).title(MAIN_GUI_TITLE).disableAllInteractions().create();
    RecipeSelectorItem recipeSelector = RecipeSelectorItemFactory.create(
        station, player, main, (e, recipe) -> {
          final StationMeta meta = station.getMeta();
          final String recipeKey = recipe.getKey();
          if (meta.getRecipeKey() == null) {
            meta.setRecipeKey(recipe.getKey());
            station.setMeta(meta);
            Bukkit.getPluginManager().callEvent(new StationUpdateEvent(station, player));
            return;
          }
          if (meta.getRecipeKey() != null && !meta.getRecipeKey().equals(recipeKey)) {
            meta.setProgress(0);
            meta.setRecipeKey(recipeKey);
            station.setMeta(meta);
            new BukkitRunnable() {
              @Override
              public void run() {
                Bukkit.getPluginManager()
                    .callEvent(new StationUpdateEvent(station, player));
              }
            }.runTask(plugin);
          }
        });
    MetaItem metaItem = MetaItemFactory.create(station);
    GuiItem filler = ItemStackBuilder.create(Material.RABBIT_FOOT)
        .itemModel(Material.GRAY_STAINED_GLASS_PANE)
        .displayName(Component.empty()).asGuiItem();
    BasicStationItem storageItem = new StorageItemFactory(stationService).create(station);
    main.setItem(3, storageItem.getGuiItem());
    main.setItem(4, recipeSelector.getGuiItem());
    main.setItem(5, metaItem.getGuiItem());
    main.getFiller().fill(filler);
    return new AnvilGuiProxy(main, recipeSelector);
  }

  static final class MetaItemFactory {

    private static MetaItem create(Station station) {
      final UpdatableGuiItemWrapper<Station> itemWrapper = UpdatableGuiItemWrapper.create(station,
          new Builder<Station>().setDisplayNameFunction(
                  s -> MiniMessage.miniMessage().deserialize("Station: <a>",
                      Placeholder.component("a", Component.text(s.id().toString()))))
              .setItemModelFunction( // can make this dynamic off the station key
                  s -> Material.ANVIL.getKey()).build(), null);
      return new MetaItem(itemWrapper);
    }

  }

  static final class StorageItemFactory implements IFactory<BasicStationItem, Station> {

    private final IStationService stationService;

    StorageItemFactory(IStationService stationService) {
      this.stationService = stationService;
    }

    @Override
    public @NotNull AnvilGuiProxy.BasicStationItem create(@Nullable Station station) {
      GuiItem guiItem = ItemStackBuilder.create(Material.CHEST)
          .displayName("<white>Storage").asGuiItem(e -> {
            if (station != null) {
              HumanEntity entity = e.getWhoClicked();
              Station s = stationService.getStation(station.id());
              if (s != null) {
                Inventory inventory = s.getInventory();
                entity.openInventory(inventory);
              }
            }
          });
      return new BasicStationItem(guiItem);
    }
  }

  static final class RecipeSelectorItemFactory {

    private static final int ROWS = 4;
    private static final int PAGE_SIZE = 9 * (ROWS - 1);
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

    private static PaginatedGui createGui(Component title) {
      return Gui.paginated().disableAllInteractions().title(title).rows(ROWS).pageSize(PAGE_SIZE)
          .create();
    }

    @SuppressWarnings("UnstableApiUsage")
    private static GuiWrapper<SmaugRecipe, PaginatedGui> createCodexGuiWrapper(Station station) {
      List<SmaugRecipe> recipes = Smaug.fetchAllRecipes(station.stationKey());
      ItemFactory<SmaugRecipe> itemFactory = new Builder<SmaugRecipe>().setDisplayNameFunction(
              RecipeSelectorItemFactory::recipeItemHeader)
          .setItemModelFunction(RecipeSelectorItemFactory::retrieveItemModel)
          .setLoreFunction(recipe -> {
            final IngredientList ingredientList = recipe.getIngredients();
            ItemLore.Builder builder = ItemLore.lore().addLine(INGREDIENT_TEXT)
                .addLines(ingredientList.components());
            IngredientList missing = ingredientList.findMissing(
                station.getMeta().getInventory().getContents());
            if (!missing.isEmpty()) {
              builder.addLine(MiniMessage.miniMessage()
                      .deserialize("<italic:false><white>Missing Ingredients:"))
                  .addLines(missing.components());
            }
            return builder.build();
          }).build();
      return GuiWrapper.create(
          createGui(Component.text("Codex")),
          recipes, itemFactory).build();
    }

    private static GuiWrapper<SmaugRecipe, PaginatedGui> createRecipeSelectorGuiWrapper(
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
      return GuiWrapper.create(
          createGui(Component.text("Recipes")), recipes,
          itemFactory).setClickEventConsumer(recipeBiConsumer).build();
    }

    private static GuiItem guiReferenceItem(ItemStack stack, BaseGui gui) {
      return new GuiItem(stack, e -> {
        HumanEntity entity = e.getWhoClicked();
        gui.open(entity);
      });
    }

    @SuppressWarnings("UnstableApiUsage")
    private static GuiItem createFillerItem(BaseGui gui) {
      ItemStack stack = ItemStackBuilder.create(Material.RABBIT_FOOT)
          .setData(DataComponentTypes.ITEM_MODEL, Material.GRAY_STAINED_GLASS_PANE.getKey())
          .setData(DataComponentTypes.ITEM_NAME, Component.empty()).build();
      return guiReferenceItem(stack, gui);
    }

    private static void addStaticItems(PaginatedGui gui, BaseGui mainGui) {
      final GuiItem filler = createFillerItem(mainGui);
      final int rows = gui.getRows();
      ItemStack stack = ItemStackBuilder.create(Material.RABBIT_FOOT)
          .itemModel(Material.PAPER).build();
      gui.setItem(rows, 1, ItemStackBuilder.create(stack)
          .displayName("Previous")
          .asGuiItem(e -> gui.previous()));
      gui.setItem(rows, 9, ItemStackBuilder.create(stack)
          .displayName("Next")
          .asGuiItem(e -> gui.next()));
      gui.getFiller().fillBetweenPoints(rows, 3, rows, 8, filler);
    }

    private static void createStaticItemsAndLink(GuiWrapper<?, PaginatedGui> guiWrapper,
        BaseGui mainGui,
        GuiItem item) {
      PaginatedGui gui = guiWrapper.getGui();
      final int rows = gui.getRows();
      addStaticItems(gui, mainGui);
      gui.setItem(rows, 2, item);
    }

    private static RecipeSelectorItem create(Station station, Player player, BaseGui mainGui,
        BiConsumer<InventoryClickEvent, SmaugRecipe> recipeSelectorConsumer) {
      GuiWrapper<SmaugRecipe, PaginatedGui> codexGuiWrapper = createCodexGuiWrapper(station);
      GuiWrapper<SmaugRecipe, PaginatedGui> recipeSelectorGuiWrapper = createRecipeSelectorGuiWrapper(
          station,
          recipeSelectorConsumer);

      GuiItem linkedRecipeItem = ItemStackBuilder.create(Material.RABBIT_FOOT)
          .itemModel(Material.BOOK)
          .displayName("Recipes")
          .asGuiItem(GUI_OPEN_INVENTORY_ACTION.apply(recipeSelectorGuiWrapper.getGui()));

      GuiItem linkedCodexItem = ItemStackBuilder.create(Material.RABBIT_FOOT)
          .itemModel(Material.WRITABLE_BOOK)
          .displayName("Codex")
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

    @NotNull
    static Key retrieveItemModel(@Nullable SmaugRecipe recipe) {
      if (recipe == null) {
        return Material.MAP.getKey();
      }
      ItemStack reference = recipe.getOutput().getReference();
      return reference.getDataOrDefault(DataComponentTypes.ITEM_MODEL,
          reference.getType().getKey());
    }

    static Component retrieveDisplayName(SmaugRecipe recipe) {
      final ItemStack reference = recipe.getOutput().getReference();
      final ItemMeta meta = reference.getItemMeta();
      return meta.hasDisplayName() ? meta.displayName()
          : reference.getDataOrDefault(DataComponentTypes.ITEM_NAME,
              Component.text("def"));
    }
  }
}
