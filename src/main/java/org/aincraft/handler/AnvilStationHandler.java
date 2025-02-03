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

package org.aincraft.handler;

import com.google.common.base.Preconditions;
import dev.triumphteam.gui.guis.Gui;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.aincraft.Smaug;
import org.aincraft.api.event.TrackableProgressUpdateEvent;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.anvil.MetaStationPlayerModel;
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.display.IViewModel.IViewModelBinding;
import org.aincraft.container.gui.AnvilGuiProxy;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.ItemIdentifier;
import org.aincraft.container.item.ItemStackBuilder;
import org.aincraft.database.model.meta.TrackableProgressMeta;
import org.aincraft.database.model.meta.TrackableProgressMeta.StationInventory;
import org.aincraft.database.model.meta.TrackableProgressMeta.StationInventory.ItemAddResult;
import org.aincraft.database.model.test.IMetaStation;
import org.aincraft.database.model.test.IStation;
import org.aincraft.exception.ForwardReferenceException;
import org.aincraft.exception.UndefinedRecipeException;
import org.aincraft.listener.IMetaStationDatabaseService;
import org.aincraft.listener.StationServiceLocator.IStationFacade;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public final class AnvilStationHandler implements IStationHandler {

  private final NamespacedKey idKey;
  private final Function<PlayerInteractEvent, IMetaStation<TrackableProgressMeta>> eventToContextFunction;
  private final IViewModel<IMetaStation<TrackableProgressMeta>> viewModel;
  private final BossBarManager bossBarManager;

  public AnvilStationHandler(
      NamespacedKey idKey,
      Function<PlayerInteractEvent, IMetaStation<TrackableProgressMeta>> eventToContextFunction,
      IViewModel<IMetaStation<TrackableProgressMeta>> viewModel) {
    this.idKey = idKey;
    this.eventToContextFunction = eventToContextFunction;
    this.viewModel = viewModel;
    this.bossBarManager = new BossBarManager(viewModel);
  }


  @Override
  public void handle(final PlayerInteractEvent event) {
    IMetaStation<TrackableProgressMeta> station = eventToContextFunction.apply(event);
    TrackableProgressMeta meta = station.getMeta();
    Player player = event.getPlayer();
    ItemStack item = event.getItem();

    final MetaStationPlayerModel<TrackableProgressMeta> proxy = new MetaStationPlayerModel<>(
        player, station);
    if (event.getAction().isRightClick()) {
      event.setCancelled(true);
      if (item != null) {
        ItemAddResult result = meta.getInventory().add(List.of(item));
        if (result.isSuccess()) {
          Bukkit.getPluginManager()
              .callEvent(new TrackableProgressUpdateEvent(
                  station.setMeta(m -> m.setInventory(result.getInventory())), player));
          player.sendMessage(Component.empty().color(
                  NamedTextColor.WHITE).append(Component.text("Deposited:"))
              .append(item.displayName()));
        }
      } else {
        //openMenu(guiViewModel, proxy);
      }
      return;
    }
    if (player.isSneaking() || !ItemIdentifier.contains(item, idKey, "hammer")) {
      return;
    }
    event.setCancelled(true);
    StationInventory inventory = meta.getInventory();
    List<SmaugRecipe> recipes = Smaug.fetchAllRecipes(station, inventory.getContents());
    if (recipes.isEmpty()) {
      player.sendMessage("There are not any recipes available");
      return;
    }
    SmaugRecipe recipe = select(station,
        recipes, player);
    if (recipe == null || !recipe.test(inventory.getContents()).isSuccess()) {
      return;
    }
    final Location stationBlockLocation = station.getBlockLocation();
    if (recipe.getActions() > 0) {
      bossBarManager.show(proxy);
      if (meta.getProgress() < recipe.getActions()) {
        successfulAction(stationBlockLocation);
        IMetaStation<TrackableProgressMeta> s = station.setMeta(
            m -> m.setProgress(progress -> progress + 1));
        Bukkit.getPluginManager()
            .callEvent(new TrackableProgressUpdateEvent(s
                , player));
      } else {
        ItemStack stack = craftRecipeOutput(recipe);
        ItemAddResult result = inventory.setItems(recipe.getIngredients()
            .remove(inventory.getItems())).add(List.of(stack));
        if (result.isSuccess()) {
          player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
          Bukkit.getPluginManager()
              .callEvent(new TrackableProgressUpdateEvent(station.setMeta(m -> {
                    m.setRecipeKey(null);
                    m.setProgress(0);
                    m.setInventory(result.getInventory());
                  }
              ), player));
        }
      }
    } else {
////      final IngredientList ingredientList = recipe.getIngredients();
////      final Map<Integer, ItemStack> removed = ingredientList.remove(inventory.getItems());
////      final ItemAddResult result = inventory.setItems(removed).add(craftRecipeOutput(recipe));
////      if (result.isSuccess()) {
////        station.setMeta(
////            m -> m.setRecipeKey(null).setProgress(0).setInventory(result.getInventory()));
////
////      }
    }
  }

  @NotNull
  private static ItemStack craftRecipeOutput(SmaugRecipe recipe) {
    final IKeyedItem item = recipe.getOutput();
    final ItemStack reference = item.getReference();
    return ItemStackBuilder.create(reference).setAmount(recipe.getAmount()).build();
  }

  private static void openMenu(
      IViewModel<MetaStationPlayerModel<TrackableProgressMeta>> viewModel,
      MetaStationPlayerModel<TrackableProgressMeta> proxy) {
    IViewModelBinding binding = viewModel.getBinding(
        proxy);
    if (binding == null) {
      return;
    }
    Gui gui = binding.getProperty(Gui.class);
    if (gui != null) {
      gui.open(proxy.player());
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

  private SmaugRecipe select(IMetaStation<TrackableProgressMeta> mutableStation,
      List<SmaugRecipe> recipes,
      Player player) {
    final TrackableProgressMeta meta = mutableStation.getMeta();
    final String recipeKey = meta.getRecipeKey();
    if (recipeKey != null) {
      try {
        return Smaug.fetchRecipe(recipeKey);
      } catch (ForwardReferenceException | UndefinedRecipeException e) {
        throw new RuntimeException(e);
      }
    }
    int size = recipes.size();
    if (size > 1) {
//      IViewModelBinding binding = guiViewModel.getBinding(
//          new StationPlayerModelProxy<>(player, station));
//      RecipeSelectorItem selectorItem = binding.getProperty("recipe-selector",
//          RecipeSelectorItem.class);
//      selectorItem.recipeSelectorGui().open(player);
    }
    if (size == 1) {
      SmaugRecipe recipe = recipes.getFirst();
      mutableStation.setMeta(
          m -> {
            m.setProgress(0);
            m.setRecipeKey(recipe.getKey());
          });
      Bukkit.getPluginManager().callEvent(new TrackableProgressUpdateEvent(mutableStation, player));
      return recipe;
    }
    return null;
  }

  static final class BossBarManager {

    private final Map<MetaStationPlayerModel<TrackableProgressMeta>, Integer> bossBarTaskMap = new HashMap<>();
    private final IViewModel<IMetaStation<TrackableProgressMeta>> viewModel;

    BossBarManager(IViewModel<IMetaStation<TrackableProgressMeta>> viewModel) {
      this.viewModel = viewModel;
    }

    private void show(MetaStationPlayerModel<TrackableProgressMeta> proxy) {
      IViewModelBinding binding = viewModel.getBinding(proxy.station());
      if (binding != null) {
        BossBar bossBar = binding.getProperty(BossBar.class);
        if (bossBar != null) {
          Player player = proxy.player();
          if (!playerIsViewingBossBar(player, bossBar)) {
            player.showBossBar(bossBar);
          }
          if (bossBarTaskMap.containsKey(proxy)) {
            Integer previousTaskId = bossBarTaskMap.remove(proxy);
            Bukkit.getScheduler().cancelTask(previousTaskId);
          }
          int taskId = new BukkitRunnable() {
            @Override
            public void run() {
              player.hideBossBar(bossBar);
            }
          }.runTaskLater(Smaug.getPlugin(), 20L).getTaskId();
          bossBarTaskMap.put(proxy, taskId);
        }
      }
    }

    private static boolean playerIsViewingBossBar(Player player, BossBar bossBar) {
      for (BossBar activeBossBar : player.activeBossBars()) {
        if (activeBossBar.equals(bossBar)) {
          return true;
        }
      }
      return false;
    }
  }
}
