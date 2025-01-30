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

import com.google.inject.name.Named;
import dev.triumphteam.gui.guis.Gui;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.aincraft.Smaug;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.gui.AnvilGuiProxy.RecipeSelectorItem;
import org.aincraft.container.item.ItemStackBuilder;
import org.aincraft.database.model.meta.TrackableProgressMeta;
import org.aincraft.exception.ForwardReferenceException;
import org.aincraft.exception.UndefinedRecipeException;
import org.aincraft.container.gui.AnvilGuiProxy;
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.display.IViewModel.IViewModelBinding;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.ItemIdentifier;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationInventory;
import org.aincraft.database.model.Station.StationInventory.ItemAddResult;
import org.aincraft.listener.IStationService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public final class AnvilStationHandler implements StationHandler<TrackableProgressMeta> {

  private final IStationService service;
  private final NamespacedKey idKey;

  public AnvilStationHandler(IStationService service,
      @Named("id") NamespacedKey idKey) {
    this.service = service;
    this.idKey = idKey;
  }

  @Override
  public void handle(final Context<TrackableProgressMeta> ctx) {
    final Player player = ctx.getPlayer();
    final ItemStack item = ctx.getItem();
    final Station<TrackableProgressMeta> station = ctx.getStation();
    final StationPlayerModelProxy<TrackableProgressMeta> proxy = new StationPlayerModelProxy<>(
        player, station);
    if (ctx.isRightClick()) {
      ctx.cancel();
      if (item != null) {
        ItemAddResult result = station.getMeta().getInventory().add(List.of(item));
        if (result.isSuccess()) {
          Bukkit.getPluginManager()
              .callEvent(new StationUpdateEvent<>(
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
    ctx.cancel();
    TrackableProgressMeta meta = station.getMeta();
    StationInventory inventory = meta.getInventory();
    List<SmaugRecipe> recipes = Smaug.fetchAllRecipes(station, inventory.getContents());
    if (recipes.isEmpty()) {
      player.sendMessage("There are not any recipes available");
    }
    SmaugRecipe recipe = select(station,
        recipes, player);
    if (recipe == null || !recipe.test(inventory.getContents()).isSuccess()) {
      return;
    }

    final Location stationBlockLocation = station.blockLocation();
    if (recipe.getActions() > 0) {
      //bossBarManager.show(proxy);
      if (station.getMeta().getProgress() < recipe.getActions()) {
        successfulAction(stationBlockLocation);
        Bukkit.getPluginManager()
            .callEvent(new StationUpdateEvent<>(
                station.setMeta(m -> m.setProgress(progress -> progress + 1)), player));
      } else {
        ItemStack stack = craftRecipeOutput(recipe);
        ItemAddResult result = inventory.setItems(recipe.getIngredients()
            .remove(inventory.getItems())).add(List.of(stack));
        if (result.isSuccess()) {
          player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
          Bukkit.getPluginManager()
              .callEvent(
                  new StationUpdateEvent<>(station.setMeta(m -> m.toBuilder()
                      .setRecipeKey(null)
                      .setProgress(0)
                      .setInventory(result.getInventory())),
                      player));
        }
      }
    } else {
//      final IngredientList ingredientList = recipe.getIngredients();
//      final Map<Integer, ItemStack> removed = ingredientList.remove(inventory.getItems());
//      final ItemAddResult result = inventory.setItems(removed).add(craftRecipeOutput(recipe));
//      if (result.isSuccess()) {
//        station.setMeta(
//            m -> m.setRecipeKey(null).setProgress(0).setInventory(result.getInventory()));
//
//      }
    }
  }

  @NotNull
  private static ItemStack craftRecipeOutput(SmaugRecipe recipe) {
    final IKeyedItem item = recipe.getOutput();
    final ItemStack reference = item.getReference();
    return ItemStackBuilder.create(reference).setAmount(recipe.getAmount()).build();
  }
//
//  private static void openMenu(IViewModel<StationPlayerModelProxy, AnvilGuiProxy> viewModel,
//      StationPlayerModelProxy proxy) {
//    IViewModelBinding binding = viewModel.getBinding(
//        proxy);
//    if (binding == null) {
//      return;
//    }
//    Gui gui = binding.getProperty(Gui.class);
//    if (gui != null) {
//      gui.open(proxy.player());
//    }
//  }

  private static void successfulAction(
      @NotNull Location stationLocation) {
    World world = stationLocation.getWorld();
    assert world != null;
    world.playSound(stationLocation, Sound.BLOCK_ANVIL_USE, 1f, 1f);
    world.spawnParticle(Particle.LAVA, stationLocation.clone().add(0.5, 1, 0.5), 1, 0, 0, 0, 0,
        null);
  }

  private SmaugRecipe select(Station<TrackableProgressMeta> station, List<SmaugRecipe> recipes, Player player) {
    final TrackableProgressMeta meta = station.getMeta();
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
      station.setMeta(m -> m.toBuilder().setProgress(0).setRecipeKey(recipe.getKey()));
      Bukkit.getPluginManager().callEvent(new StationUpdateEvent(station, player));
      return recipe;
    }
    return null;
  }

  @Override
  public @NotNull Key key() {
    return null;
  }

  static final class BossBarManager {

    private final Map<StationPlayerModelProxy, Integer> bossBarTaskMap = new HashMap<>();
    private final IViewModel<Station, BossBar> viewModel;

    BossBarManager(IViewModel<Station, BossBar> viewModel) {
      this.viewModel = viewModel;
    }

    private void show(StationPlayerModelProxy proxy) {
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
