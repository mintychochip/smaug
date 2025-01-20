package org.aincraft.inject.implementation.view;

import static org.aincraft.container.display.AnvilConstants.ANVIL_STATION_KEY;

import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.components.GuiType;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.aincraft.Smaug;
import org.aincraft.api.event.RecipeProgressUpdateEvent;
import org.aincraft.container.Result.Status;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.display.AnvilGuiProxy;
import org.aincraft.container.display.AnvilGuiProxy.RecipeSelectorItem;
import org.aincraft.container.gui.RecipeGui;
import org.aincraft.database.model.RecipeProgress;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationInventory;
import org.aincraft.listener.IStationService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
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
    final StationInventory inventory = stationService.getInventory(station.getId());
    Gui main = Gui.gui(GUI_TYPE).title(MAIN_GUI_TITLE).disableAllInteractions().create();
    RecipeSelectorItem item = new RecipeSelectorItemFactory(stationService).create(
        station, inventory, player, (e, recipe) -> {
          final UUID id = station.getId();
          CompletableFuture.supplyAsync(() -> stationService.getRecipeProgress(id))
              .thenAcceptAsync(progress -> {
                final String recipeKey = recipe.getKey();
                if (progress == null) {
                  stationService.createRecipeProgress(id, recipeKey);
                  return;
                }
                if (!progress.getRecipeKey().equals(recipeKey)) {
                  progress.setProgress(5);
                  progress.setRecipeKey(recipeKey);
                  new BukkitRunnable() {
                    @Override
                    public void run() {
                      Bukkit.getPluginManager()
                          .callEvent(new RecipeProgressUpdateEvent(progress,player));
                    }
                  }.runTask(plugin);
                }
              });
        });
    main.setItem(6, item.item());
    return new AnvilGuiProxy(main, item);
  }

  static final class RecipeSelectorItemFactory {

    private final IStationService stationService;

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

    private RecipeSelectorItemFactory(IStationService stationService) {
      this.stationService = stationService;
    }

    @SuppressWarnings("UnstableApiUsage")
    RecipeSelectorItem create(Station station, StationInventory inventory, Player player,
        BiConsumer<InventoryClickEvent, SmaugRecipe> recipeSelectorConsumer) {
      final RecipeProgress progress = stationService.getRecipeProgress(station.getId());
      final ItemStack stack = new ItemStack(Material.RABBIT_FOOT);
      stack.setData(DataComponentTypes.ITEM_MODEL,
          retrieveRecipeProgressItemModel(progress));
      stack.setData(DataComponentTypes.ITEM_NAME, Component.text("test"));
      final List<SmaugRecipe> recipes = retrieveAllAvailableRecipes(inventory, player);
      PaginatedGui recipeSelectorGui = RecipeGui.create(recipes,
          Component.text("Recipes:"), recipeSelectorConsumer);
      PaginatedGui allRecipeGui = RecipeGui.create(Smaug.fetchAllRecipes(ANVIL_STATION_KEY),
          Component.text("Codex:"), null);
      recipeSelectorGui.setItem(RecipeGui.ROWS, 2,
          new GuiItem(createStack(CODEX_ITEM_MODEL),
              OPEN_INVENTORY_ACTION.apply(allRecipeGui)));
      allRecipeGui.setItem(RecipeGui.ROWS, 2,
          new GuiItem(createStack(RECIPE_SELECTOR_ITEM_MODEL),
              OPEN_INVENTORY_ACTION.apply(recipeSelectorGui)));
      return new RecipeSelectorItem(new GuiItem(stack, e -> {
        HumanEntity entity = e.getWhoClicked();
        if (e.isLeftClick()) {
          recipeSelectorGui.open(entity);
        } else {
          allRecipeGui.open(entity);
        }
      }), recipeSelectorGui, allRecipeGui);
    }

    static List<SmaugRecipe> retrieveAllAvailableRecipes(StationInventory inventory,
        Player player) {
      return Smaug.fetchAllRecipes(recipe -> {
        boolean hasPermission =
            recipe.getPermission() != null && player.hasPermission(recipe.getPermission());
        boolean isValidStation = recipe.getStationKey().equals(ANVIL_STATION_KEY)
            && recipe.test(inventory.getContents()).getStatus() == Status.SUCCESS;

        return isValidStation;
      });
    }

    static Key retrieveRecipeProgressItemModel(RecipeProgress progress) {
      if (progress != null) {
        SmaugRecipe recipe = Smaug.fetchRecipe(progress.getRecipeKey());
        if (recipe != null) {
          ItemStack reference = recipe.getOutput().getReference();
          @SuppressWarnings("UnstableApiUsage")
          Key key = reference.getDataOrDefault(DataComponentTypes.ITEM_MODEL,
              reference.getType().getKey());
          return key;
        }
      }
      return DEFAULT_PROGRESS_ITEM_MODEL;
    }

    @SuppressWarnings("UnstableApiUsage")
    private static ItemStack createStack(Key itemModel) {
      ItemStack stack = ItemStack.of(Material.RABBIT_FOOT);
      stack.setData(DataComponentTypes.ITEM_MODEL, itemModel);
      return stack;
    }
  }
}
