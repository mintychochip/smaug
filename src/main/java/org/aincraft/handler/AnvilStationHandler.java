package org.aincraft.handler;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.triumphteam.gui.guis.PaginatedGui;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.aincraft.Smaug;
import org.aincraft.api.event.RecipeProgressUpdateEvent;
import org.aincraft.api.event.StationUpdateInventoryEvent;
import org.aincraft.container.Result;
import org.aincraft.container.Result.Status;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.StationHandler;
import org.aincraft.container.gui.RecipeGui;
import org.aincraft.container.gui.StationInventoryGui;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.ItemIdentifier;
import org.aincraft.database.model.RecipeProgress;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationInventory;
import org.aincraft.database.model.StationInventory.ItemAddResult;
import org.aincraft.listener.IStationService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AnvilStationHandler implements StationHandler {

  private final IStationService service;
  private final NamespacedKey idKey;

  private static final Key STATION_KEY = Key.key("smaug:anvil");


  @Inject
  public AnvilStationHandler(IStationService service,
      @Named("id") NamespacedKey idKey) {
    this.service = service;
    this.idKey = idKey;
  }

  @Override
  public void handleInteraction(IInteractionContext ctx, Consumer<SmaugRecipe> recipeConsumer) {
    final Station station = ctx.getStation();
    final Player player = ctx.getPlayer();
    final ItemStack stack = ctx.getItem();
    StationInventory inventory =
        service.hasInventory(station.getId()) ? service.getInventory(station.getId())
            : service.createInventory(station.getId(), 5);
    final RecipeProgress recipeProgress = service.getRecipeProgress(station.getId());
    if (ctx.getAction().isRightClick()) {
      ctx.cancel();
      if (stack != null) {
        ItemAddResult result = inventory.addItems(List.of(stack), remain -> {
        });
        if (result.getStatus() == Result.Status.SUCCESS) {
          Bukkit.getPluginManager()
              .callEvent(new StationUpdateInventoryEvent(station, result.getInventory()));
          player.sendMessage(Component.empty().color(
                  NamedTextColor.WHITE).append(Component.text("Deposited:"))
              .append(stack.displayName()));
        }
      } else {
        StationInventoryGui gui = new StationInventoryGui(Smaug.getPlugin(), inventory, station);
        player.openInventory(gui.getInventory());
      }
    } else {
      if (!player.isSneaking()) {
        if (!ItemIdentifier.contains(stack, idKey, "hammer")) {
          return;
        }

        ctx.cancel();
        List<SmaugRecipe> recipes = Smaug.fetchAllRecipes(
            recipe -> recipe.getStationKey().equals(station.getStationKey())
                && recipe.test(inventory.getContents()).getStatus() == Status.SUCCESS);
        if (recipes.isEmpty()) {
          player.sendMessage("There are not any recipes available");
        }
        SmaugRecipe selectedRecipe = new RecipeSelector(Smaug.getStationService()).select(station,
            recipes, player);
        if (selectedRecipe != null) {
//          if (!inventory.canAddItem(selectedRecipe.craft())) {
//            player.sendMessage("The output is full");
//            return;
//          }
          recipeConsumer.accept(selectedRecipe);
        }
      }
    }
  }

  @Override
  public void handleAction(IActionContext ctx) {
    final Station station = ctx.getStation();

    final SmaugRecipe recipe = ctx.getRecipe();
    final Player player = ctx.getPlayer();
    final StationInventory stationInventory = service.getInventory(station.getId());
    Map<Integer, ItemStack> stackMap = stationInventory.getMap();
    if (Status.FAILURE == recipe.test(stationInventory.getContents())
        .getStatus()) {
      return;
    }
    final Location location = station.getBlockLocation();
    if (recipe.getActions() > 0) {
      final RecipeProgress recipeProgress = service.getRecipeProgress(station.getId());
      float progress = recipeProgress.getProgress();
      if (progress < recipe.getActions()) {
        successfulAction(location);
        recipeProgress.increment(1);
        Bukkit.getPluginManager()
            .callEvent(new RecipeProgressUpdateEvent(recipeProgress,player));
      } else {
        Map<Integer, ItemStack> removed = recipe.getIngredients().remove(stackMap);
        IKeyedItem item = recipe.getOutput();
        ItemStack reference = item.getReference();
        ItemStack stack = new ItemStack(reference);
        stack.setAmount(recipe.getAmount());
        ItemAddResult result = stationInventory.setItems(removed)
            .addItems(List.of(stack), remaining -> {
            });
        if (result.getStatus() == Status.SUCCESS) {
          Bukkit.getPluginManager()
              .callEvent(new StationUpdateInventoryEvent(station, result.getInventory()));
          service.deleteRecipeProgress(station.getId());
        }
      }
    }
  }

  private static void successfulAction(
      @NotNull Location stationLocation) {
    World world = stationLocation.getWorld();
    assert world != null;
    world.playSound(stationLocation, Sound.BLOCK_ANVIL_USE, 1f, 1f);
    world.spawnParticle(Particle.LAVA, stationLocation.clone().add(0.5, 1, 0.5), 1, 0, 0, 0, 0,
        null);
  }


  static final class SubMenu {

    private final IStationService stationService;

    SubMenu(IStationService stationService) {
      this.stationService = stationService;
    }
//    public BaseGui create(Station station) {
//      Gui gui = Gui.gui(GuiType.DISPENSER).title(Component.text("Menu")).disableAllInteractions()
//          .create();
//      RecipeProgress recipeProgress = Smaug.getStationService().getRecipeProgress(station.getId());
//      if(recipeProgress == null) {
//
//      }
//      GuiItem item = new GuiItem(Material.BOOK, e -> {
//        Player player = (Player) e.getWhoClicked();
//        UUID id = station.getId();
//        if (!stationService.hasInventory(id)) {
//          return;
//        }
//        final StationInventory inventory = stationService.getInventory(id);
//        final List<SmaugRecipe> recipes = Smaug.fetchAllRecipes(
//            recipe -> recipe.test(player, inventory.getContents()).getStatus() == Status.SUCCESS
//                && recipe.getStationKey().equals(STATION_KEY));
//        if (recipes.isEmpty()) {
//          player.sendMessage("There aren't any available recipes");
//          return;
//        }
//        BaseGui recipeGui = createRecipeGui(recipes, gui,
//            (event, r) -> {
//              final String recipeKey = r.getKey();
//              RecipeProgress recipeProgress = stationService.getRecipeProgress(id);
//              if (recipeProgress == null) {
//                stationService.createRecipeProgress(id, recipeKey);
//              } else {
//                if (!recipeKey.equals(recipeProgress.getRecipeKey())) {
//                  stationService.updateRecipeProgress(id, progress -> {
//                    progress.setRecipeKey(recipeKey);
//                    progress.setProgress(0);
//                  });
//                }
//              }
//              player.closeInventory(Reason.PLUGIN);
//            });
//        recipeGui.open(player);
//      });
//      gui.addItem(item);
//      return gui;
//    }
//
//    private static BaseGui createRecipeGui(List<SmaugRecipe> recipes, BaseGui previous,
//        BiConsumer<InventoryClickEvent, SmaugRecipe> biConsumer) {
//      final PaginatedGui gui = RecipeGui.create(recipes, biConsumer);
//      final int rows = gui.getRows();
//      gui.setItem(rows, 8, new GuiItem(Material.ENDER_PEARL, e -> {
//        HumanEntity entity = e.getWhoClicked();
//        previous.open(entity);
//      }));
//      return gui;
//    }

    static final class AnvilMenu {


    }

  }

//  //recipe item is the item that holds the gui of the recipe selector
//  static final class RecipeItem {
//    private static Key DEFAULT_RECIPE_ITEM_MODEL = Material.MAP.getKey();
//
//    private static Component
//    private static List<Component> RECIPE_ITEM_LORE;
//    static {
//      RECIPE_ITEM_LORE =
//    }
//    private static GuiItem createRecipeItem(SmaugRecipe recipe) {
//      final ItemStack reference = recipe.getOutput().getReference();
//      ItemMeta meta = reference.getItemMeta();
//      ItemStack stack = new ItemStack(Material.RABBIT_FOOT);
//      stack.setData(DataComponentTypes.ITEM_NAME,Component.text("Recipe"));
//      stack.setData(DataComponentTypes.ITEM_MODEL,Material.ANVIL.getKey());
//      stack.setData(DataComponentTypes.LORE,Material.);
//      ItemLore.lore()
//    }
//
//    private static List<SmaugRecipe> availableRecipes(Player player, List<ItemStack> contents) {
//      return Smaug.fetchAllRecipes(
//          recipe -> recipe.getStationKey().equals(STATION_KEY)
//              && recipe.test(player, contents).getStatus() == Status.SUCCESS);
//    }
//  }

  private record RecipeSelector(IStationService service) {

    public SmaugRecipe select(Station station, List<SmaugRecipe> recipes, Player player) {
      RecipeProgress recipeProgress = service.getRecipeProgress(station.getId());
      if (recipeProgress != null) {
        String recipeKey = recipeProgress.getRecipeKey();
        return Smaug.fetchRecipe(recipeKey);
      }
      int size = recipes.size();
      if (size > 1) {
        PaginatedGui gui = RecipeGui.create(recipes, Component.text("Recipes"), (e, r) -> {
          service.createRecipeProgress(station.getId(), r.getKey());
        });
        gui.open(player);
      }
      if (size == 1) {
        SmaugRecipe recipe = recipes.getFirst();
        service.createRecipeProgress(station.getId(), recipe.getKey());
        return recipe;
      }
      return null;
    }
  }
}
