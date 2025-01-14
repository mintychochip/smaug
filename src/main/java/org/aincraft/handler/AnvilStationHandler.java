package org.aincraft.handler;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.aincraft.container.IRecipeFetcher;
import org.aincraft.container.RecipeResult;
import org.aincraft.container.RecipeResult.Status;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.StationHandler;
import org.aincraft.container.gui.RecipeMenu;
import org.aincraft.container.ingredient.Ingredient;
import org.aincraft.container.item.ItemIdentifier;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationInventory;
import org.aincraft.database.model.StationInventory.InventoryType;
import org.aincraft.database.model.StationRecipeProgress;
import org.aincraft.listener.IStationService;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AnvilStationHandler implements StationHandler {

  private final IRecipeFetcher fetcher;
  private final IStationService service;
  private final Plugin plugin;
  private final NamespacedKey idKey;

  @Inject
  public AnvilStationHandler(IRecipeFetcher fetcher, IStationService service, Plugin plugin,
      @Named("id") NamespacedKey idKey) {
    this.fetcher = fetcher;
    this.service = service;
    this.plugin = plugin;
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

    if (ctx.getAction().isRightClick()) {
      ctx.cancel();
      if (stack.getType().isAir()) {
        return;
      }
      this.deposit(inventory, stack, player, player.isSneaking(), (success, items) -> {
        Component message;
        if (success) {
          message = Component.empty().color(
                  NamedTextColor.WHITE).append(Component.text("Deposited:"))
              .append(stack.displayName());
          new BukkitRunnable() {
            @Override
            public void run() {
              for (ItemStack item : items) {
                player.getInventory().removeItem(item);
              }
            }
          }.runTask(plugin);
        } else {
          message = Component.text("Failed to deposit items");
        }
        player.sendMessage(message);
      });
    } else {
      if (!player.isSneaking()) {
        if (!ItemIdentifier.contains(stack, idKey, "hammer")) {
          return;
        }

        ctx.cancel();
        List<SmaugRecipe> recipes = fetcher.all(
            recipe -> recipe.getStationKey().equals(station.getKey())
                && recipe.test(player, inventory.getItems(InventoryType.INPUT)).getStatus()
                == Status.SUCCESS);
        if (recipes.isEmpty()) {
          player.sendMessage("There are not any recipes available");
        }
        SmaugRecipe selectedRecipe = new RecipeSelector(service, fetcher, plugin).select(station,
            recipes, player);
        if (selectedRecipe != null) {
          if (!inventory.canAddItem(selectedRecipe.craft(), InventoryType.OUTPUT)) {
            player.sendMessage("The output is full");
            return;
          }
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
    List<ItemStack> stacks = stationInventory.getItems(InventoryType.INPUT);
    if (RecipeResult.Status.FAILURE == recipe.test(player,stacks).getStatus()) {
      return;
    }
    final Location location = station.getLocation();
    if (recipe.getActions() > 0) {
      final StationRecipeProgress recipeProgress = service.getRecipeProgress(station.getId());
      int progress = recipeProgress.getProgress();
      if (progress < recipe.getActions()) {
        player.spawnParticle(Particle.LAVA, location.clone().add(0.5, 1, 0.5), 0, 0, 0, 0, null);
        recipeProgress.setProgress(progress + 1);
        service.updateRecipeProgress(recipeProgress);
      } else {
        ItemStack craft = recipe.craft();
        stationInventory.addItem(craft, InventoryType.OUTPUT);
        CompletableFuture.supplyAsync(() -> service.updateInventory(stationInventory))
            .thenAcceptAsync(b -> {
              if (b) {
                new BukkitRunnable() {
                  @Override
                  public void run() {
                    for (Ingredient i : recipe.getIngredients()) {
                      i.remove(player, stacks);
                    }
                    stationInventory.setItems(stacks, InventoryType.INPUT);
                    CompletableFuture.runAsync(() -> {
                      service.updateInventory(stationInventory);
                      service.deleteRecipeProgress(station.getId());
                    });
                  }
                }.runTask(plugin);
              }
            });
      }
    }
//    } else {
//      ItemStack stack = new ItemStack(recipe.getOutput().getReference());
//      stack.setAmount(recipe.getAmount());
//      StationInventory inventory = service.getInventory(station.getId());
//      inventory.addItem(stack, InventoryType.OUTPUT);
//      service.updateInventoryAsync(inventory, bool -> {
//        service.deleteRecipeProgress(station.getId());
//      });
//    }
  }

  private void deposit(StationInventory stationInventory, ItemStack item,
      Player player, boolean bulk, BiConsumer<Boolean, Iterable<ItemStack>> callback) {

    PlayerInventory playerInventory = player.getInventory();

    List<ItemStack> stacks = bulk ? Arrays
        .stream(playerInventory.getContents())
        .filter(i -> i != null && i.isSimilar(item))
        .toList() : List.of(item);

    if (!stationInventory.canAddItems(stacks, InventoryType.INPUT)) {
      callback.accept(false, stacks);
      return;
    }

    stationInventory.addItems(stacks, InventoryType.INPUT);

    service.updateInventoryAsync(stationInventory, success -> callback.accept(success, stacks));
  }

  private record RecipeSelector(IStationService service, IRecipeFetcher recipeFetcher,
                                Plugin plugin) {

    public SmaugRecipe select(Station station, List<SmaugRecipe> recipes, Player player) {
      StationRecipeProgress recipeProgress = service.getRecipeProgress(station.getId());
      if (recipeProgress != null) {
        String recipeKey = recipeProgress.getRecipeKey();
        return recipeFetcher.fetch(recipeKey);
      }
      int size = recipes.size();
      if (size > 1) {
        RecipeMenu menu = new RecipeMenu(plugin, recipes, station, recipe -> {
          service.createRecipeProgress(station.getId(), recipe.getKey());
        });
        recipes.forEach(menu::addButton);
        player.openInventory(menu.getInventory());
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
