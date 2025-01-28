package org.aincraft.inject.implementation.view;

import static org.aincraft.container.display.AnvilConstants.ANVIL_STATION_KEY;

import com.google.common.base.Preconditions;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.components.GuiType;
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
import org.aincraft.container.gui.AnvilGuiProxy.GuiItemWrapper;
import org.aincraft.container.gui.AnvilGuiProxy.GuiWrapper;
import org.aincraft.container.gui.AnvilGuiProxy.RecipeSelectorItem;
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
    RecipeSelectorItem item = RecipeSelectorItemFactory.create(
        station, player, main, (e, recipe) -> {
          final StationMeta meta = station.getMeta();
          final String recipeKey = recipe.getKey();
//          if (meta.getRecipeKey() == null) {
//            meta.setRecipeKey(recipe.getKey());
//            station.setMeta(meta);
//            stationService.updateStation(station);
//            return;
//          }
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
    main.setItem(6, item.getGuiItem());
    return new AnvilGuiProxy(main, item);
  }

  static final class StorageItemFactory {

//    static StorageItem create(Station station) {
//      return new StorageItem(GuiItemWrapper.create(station, s -> ItemStackBuilder.create(Material.RABBIT_FOOT)
//          .displayName("<gold>Storage").build(),e -> {
//        HumanEntity entity = e.getWhoClicked();
//        entity.openInventory(station.getInventory());
//      }),station.getInventory());
//    }
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
    private static GuiWrapper<SmaugRecipe, PaginatedGui> codexGuiWrapper(Station station) {
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

    private static GuiWrapper<SmaugRecipe, PaginatedGui> recipeSelectorGuiWrapper(Station station,
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

    private static RecipeSelectorItem create(Station station, Player player, BaseGui mainGui,
        BiConsumer<InventoryClickEvent, SmaugRecipe> recipeSelectorConsumer) {
      StationMeta meta = station.getMeta();
      GuiWrapper<SmaugRecipe, PaginatedGui> codexGuiWrapper = codexGuiWrapper(station);
      GuiWrapper<SmaugRecipe, PaginatedGui> recipeSelectorGuiWrapper = recipeSelectorGuiWrapper(
          station,
          recipeSelectorConsumer);
      codexGuiWrapper.setUpdateConsumer(wrapper -> {
        PaginatedGui gui = wrapper.getGui();
        gui.clearPageItems();
        int rows = gui.getRows();
        addStaticItems(gui, mainGui);
        gui.setItem(rows, 2,
            ItemStackBuilder.create(Material.RABBIT_FOOT).itemModel(Material.BOOK)
                .displayName("Recipes")
                .asGuiItem(GUI_OPEN_INVENTORY_ACTION.apply(recipeSelectorGuiWrapper.getGui())));
      });
      recipeSelectorGuiWrapper.setUpdateConsumer(wrapper -> {
        PaginatedGui gui = wrapper.getGui();
        int rows = gui.getRows();
        gui.clearPageItems();
        addStaticItems(gui, mainGui);
        gui.setItem(rows, 2,
            ItemStackBuilder.create(Material.RABBIT_FOOT).itemModel(Material.WRITABLE_BOOK)
                .displayName("Codex")
                .asGuiItem(GUI_OPEN_INVENTORY_ACTION.apply(codexGuiWrapper.getGui())));
      });
      final SmaugRecipe recipe = Smaug.fetchRecipe(meta.getRecipeKey());
      return new RecipeSelectorItem(GuiItemWrapper.create(recipe,
          new ItemFactory.Builder<SmaugRecipe>().setDisplayNameFunction(r -> {
                if (r == null) {
                  return Component.text("No Recipe Selected");
                }
                return MiniMessage.miniMessage().deserialize("Selected: <a>",
                    Placeholder.component("a", retrieveDisplayName(r)));
              })
              .setItemModelFunction(RecipeSelectorItemFactory::retrieveItemModel).build(), e -> {
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

    static List<SmaugRecipe> retrieveAllAvailableRecipes(StationMeta meta) {
      StationInventory inventory = meta.getInventory();
      return Smaug.fetchAllRecipes(recipe -> recipe.getStationKey().equals(ANVIL_STATION_KEY)
          && recipe.test(inventory.getContents()).getStatus() == Status.SUCCESS);
    }
  }
}
