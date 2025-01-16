package org.aincraft.handler;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.aincraft.api.event.StationInventoryEvent;
import org.aincraft.container.IRecipeFetcher;
import org.aincraft.container.Result;
import org.aincraft.container.Result.Status;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.StationHandler;
import org.aincraft.container.gui.RecipeMenu;
import org.aincraft.container.gui.StationInventoryGui;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.ItemIdentifier;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.StationInventory;
import org.aincraft.database.model.StationInventory.ItemAddResult;
import org.aincraft.database.model.StationRecipeProgress;
import org.aincraft.listener.IStationService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class AnvilStationHandler implements StationHandler {

  enum InteractionType {
    RIGHT_CLICK,
    LEFT_CLICK,
    RIGHT_SHIFT_CLICK,
    LEFT_SHIFT_CLICK
  }

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
    ctx.cancel();
    if (ctx.getAction().isRightClick()) {
      if (stack != null) {
        ItemAddResult result = inventory.addItems(List.of(stack), remain -> {
        });
        if (result.getStatus() == Result.Status.SUCCESS) {
          Bukkit.getPluginManager().callEvent(new StationInventoryEvent(result.getInventory()));
          player.sendMessage(Component.empty().color(
                  NamedTextColor.WHITE).append(Component.text("Deposited:"))
              .append(stack.displayName()));
        }
      } else {
        StationInventoryGui gui = new StationInventoryGui(plugin, inventory);
        player.openInventory(gui.getInventory());
      }
    } else {
      if (!player.isSneaking()) {
        if (!ItemIdentifier.contains(stack, idKey, "hammer")) {
          return;
        }

        ctx.cancel();
        List<SmaugRecipe> recipes = fetcher.all(
            recipe -> recipe.getStationKey().equals(station.getStationKey())
                && recipe.test(player, inventory.getContents()).getStatus()
                == Status.SUCCESS);
        if (recipes.isEmpty()) {
          player.sendMessage("There are not any recipes available");
        }
        SmaugRecipe selectedRecipe = new RecipeSelector(service, fetcher, plugin).select(station,
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
    if (Status.FAILURE == recipe.test(player, stationInventory.getContents())
        .getStatus()) {
      return;
    }
    final Location location = station.getLocation();
    if (recipe.getActions() > 0) {
      final StationRecipeProgress recipeProgress = service.getRecipeProgress(station.getId());
      int progress = recipeProgress.getProgress();
      if (progress < recipe.getActions()) {
        successfulAction(location);
        recipeProgress.setProgress(progress + 1);
        service.updateRecipeProgress(recipeProgress);
      } else {
        Map<Integer, ItemStack> removed = recipe.getIngredients().remove(player, stackMap);
        IKeyedItem item = recipe.getOutput();
        ItemStack reference = item.getReference();
        ItemStack stack = new ItemStack(reference);
        stack.setAmount(recipe.getAmount());
        ItemAddResult result = stationInventory.setItems(removed)
            .addItems(List.of(stack), remaining -> {
            });
        if (result.getStatus() == Status.SUCCESS) {
          Bukkit.getPluginManager().callEvent(new StationInventoryEvent(result.getInventory()));
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
