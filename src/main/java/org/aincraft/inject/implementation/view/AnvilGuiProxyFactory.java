package org.aincraft.inject.implementation.view;

import static org.aincraft.container.display.AnvilConstants.ANVIL_STATION_KEY;

import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.components.GuiType;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.ItemLore.Builder;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.aincraft.Smaug;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.Result.Status;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.display.AnvilGuiProxy;
import org.aincraft.container.display.AnvilGuiProxy.RecipeSelectorItem;
import org.aincraft.container.gui.RecipeGui;
import org.aincraft.container.gui.RecipeGuiFactory;
import org.aincraft.container.gui.RecipeGuiItemFactory;
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

public class AnvilGuiProxyFactory {

  private static final GuiType GUI_TYPE = GuiType.DISPENSER;
  private static final Component MAIN_GUI_TITLE = Component.text("Menu");

  private final IStationService stationService;
  private final Plugin plugin;

  public AnvilGuiProxyFactory(IStationService stationService, Plugin plugin) {
    this.stationService = stationService;
    this.plugin = plugin;
  }

  public AnvilGuiProxy create(Station station, Player player) {
    Gui main = Gui.gui(GUI_TYPE).title(MAIN_GUI_TITLE).disableAllInteractions().create();
    RecipeSelectorItem item = RecipeSelectorItemFactory.create(
        station, player, (e, recipe) -> {
          final StationMeta meta = station.getMeta();
          final String recipeKey = recipe.getKey();
          if (meta.getRecipeKey() == null) {
            meta.setRecipeKey(recipe.getKey());
            station.setMeta(meta);
            stationService.updateStation(station);
            return;
          }
          if (!meta.getRecipeKey().equals(recipeKey)) {
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
    main.setItem(6, item.item());
    return new AnvilGuiProxy(main, item);
  }

  static final class RecipeSelectorItemFactory {

    private static final Function<BaseGui, GuiAction<InventoryClickEvent>> OPEN_INVENTORY_ACTION;

    private static final Key CODEX_ITEM_MODEL;
    private static final Key RECIPE_SELECTOR_ITEM_MODEL;
    private static final Key DEFAULT_PROGRESS_ITEM_MODEL;

    static {
      OPEN_INVENTORY_ACTION = (gui) -> (GuiAction<InventoryClickEvent>) inventoryClickEvent -> {
        HumanEntity entity = inventoryClickEvent.getWhoClicked();
        gui.open(entity);
      };
      CODEX_ITEM_MODEL = Material.BOOK.getKey();
      RECIPE_SELECTOR_ITEM_MODEL = Material.WRITABLE_BOOK.getKey();
      DEFAULT_PROGRESS_ITEM_MODEL = Material.MAP.getKey();
    }

    @SuppressWarnings("UnstableApiUsage")
    private static RecipeSelectorItem create(Station station, Player player,
        BiConsumer<InventoryClickEvent, SmaugRecipe> recipeSelectorConsumer) {
      final ItemStack stack = new ItemStack(Material.RABBIT_FOOT);
      stack.setData(DataComponentTypes.ITEM_MODEL, Material.MAP.getKey());
      stack.setData(DataComponentTypes.ITEM_NAME, Component.text("test"));
      PaginatedGui codexGui = createCodexGui(Key.key("smaug:anvil"));
      PaginatedGui recipeSelectorGui = createRecipeSelectorGui(station, recipeSelectorConsumer);
      recipeSelectorGui.setItem(RecipeGui.ROWS, 2,
          new GuiItem(createStack(CODEX_ITEM_MODEL),
              OPEN_INVENTORY_ACTION.apply(codexGui)));
      codexGui.setItem(RecipeGui.ROWS, 2,
          new GuiItem(createStack(RECIPE_SELECTOR_ITEM_MODEL),
              OPEN_INVENTORY_ACTION.apply(recipeSelectorGui)));
      return new RecipeSelectorItem(new GuiItem(stack, e -> {
        HumanEntity entity = e.getWhoClicked();
        if (e.isLeftClick()) {
          recipeSelectorGui.open(entity);
        } else {
          codexGui.open(entity);
        }
      }), recipeSelectorGui, codexGui);
    }

    static Key retrieveItemModel(SmaugRecipe recipe) {
      ItemStack reference = recipe.getOutput().getReference();
      return reference.getDataOrDefault(DataComponentTypes.ITEM_MODEL,
          reference.getType().getKey());
    }

    private static PaginatedGui createRecipeSelectorGui(Station station,
        BiConsumer<InventoryClickEvent, SmaugRecipe> recipeBiConsumer) {
      final StationMeta meta = station.getMeta();
      final StationInventory stationInventory = meta.getInventory();
      final RecipeGuiItemFactory itemFactory = RecipeGuiItemFactory.create()
          .setItemModelFunction(RecipeSelectorItemFactory::retrieveItemModel)
          .setDisplayNameFunction(RecipeSelectorItemFactory::createDisplayName)
          .setLoreFunction(recipe -> {
            final IngredientList ingredientList = recipe.getIngredients();
            final IngredientList missingList = ingredientList.findMissing(
                stationInventory.getContents());
            Builder builder = ItemLore.lore().addLine(Component.text("ingredients"))
                .addLines(ingredientList.components());
            if (!missingList.isEmpty()) {
              builder.addLine(Component.text("Missing Ingredients"))
                  .addLines(missingList.components());
            }
            return builder.build();
          }).build();
      List<SmaugRecipe> recipes = Smaug.fetchAllRecipes(
          recipe -> recipe.getStationKey().equals(station.stationKey())
              && recipe.test(stationInventory.getContents()).getStatus() == Status.SUCCESS);
      return new RecipeGuiFactory(itemFactory).create(recipes, Component.text("Recipes"),
          recipeBiConsumer);
    }

    private static PaginatedGui createCodexGui(Key key) {
      final RecipeGuiItemFactory itemFactory = RecipeGuiItemFactory.create()
          .setItemModelFunction(RecipeSelectorItemFactory::retrieveItemModel)
          .setDisplayNameFunction(RecipeSelectorItemFactory::createDisplayName)
          .setLoreFunction(recipe -> {
            IngredientList ingredientList = recipe.getIngredients();
            return ItemLore.lore().addLine(Component.text("ingredients"))
                .addLines(ingredientList.components()).build();
          }).build();
      return new RecipeGuiFactory(itemFactory).create(Smaug.fetchAllRecipes(key),
          Component.text("Codex"), null);
    }

    static Component createDisplayName(SmaugRecipe recipe) {
      final ItemStack reference = recipe.getOutput().getReference();
      final ItemMeta meta = reference.getItemMeta();
      @SuppressWarnings("UnstableApiUsage")
      Component displayName = meta.hasDisplayName() ? meta.displayName()
          : reference.getDataOrDefault(DataComponentTypes.ITEM_NAME,
              Component.text("def"));
      return MiniMessage.miniMessage()
          .deserialize("Recipe: <a>", Placeholder.component("a", displayName));
    }

    static List<SmaugRecipe> retrieveAllAvailableRecipes(StationMeta meta,
        Player player) {
      StationInventory inventory = meta.getInventory();
      return Smaug.fetchAllRecipes(recipe -> {
//        boolean hasPermission =
//            recipe.getPermission() != null && player.hasPermission(recipe.getPermission());
        boolean isValidStation = recipe.getStationKey().equals(ANVIL_STATION_KEY)
            && recipe.test(inventory.getContents()).getStatus() == Status.SUCCESS;

        return isValidStation;
      });
    }

    @SuppressWarnings("UnstableApiUsage")
    private static ItemStack createStack(Key itemModel) {
      ItemStack stack = ItemStack.of(Material.RABBIT_FOOT);
      stack.setData(DataComponentTypes.ITEM_MODEL, itemModel);
      return stack;
    }
  }
}
