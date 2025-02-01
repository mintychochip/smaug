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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.aincraft.Smaug;
import org.aincraft.api.event.TrackableProgressUpdateEvent;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.display.IViewModel.IViewModelBinding;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.ItemIdentifier;
import org.aincraft.container.item.ItemStackBuilder;
import org.aincraft.database.model.MutableStation;
import org.aincraft.database.model.meta.TrackableProgressMeta;
import org.aincraft.database.model.meta.TrackableProgressMeta.StationInventory;
import org.aincraft.database.model.meta.TrackableProgressMeta.StationInventory.ItemAddResult;
import org.aincraft.exception.ForwardReferenceException;
import org.aincraft.exception.UndefinedRecipeException;
import org.aincraft.listener.IMutableStationService;
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

  private final IMutableStationService<TrackableProgressMeta> service;
  private final NamespacedKey idKey;

  public AnvilStationHandler(IMutableStationService<TrackableProgressMeta> service,
      @Named("id") NamespacedKey idKey) {
    this.service = service;
    this.idKey = idKey;
  }

  @Override
  public void handle(final Context<TrackableProgressMeta> ctx) {
    final Player player = ctx.getPlayer();
    final ItemStack item = ctx.getItem();
    final MutableStation<TrackableProgressMeta> mutableStation = ctx.getStation();
    final StationPlayerModelProxy<TrackableProgressMeta> proxy = new StationPlayerModelProxy<>(
        player, mutableStation);
    if (ctx.isRightClick()) {
      ctx.cancel();
      if (item != null) {
        ItemAddResult result = mutableStation.getMeta().getInventory().add(List.of(item));
        if (result.isSuccess()) {
          Bukkit.getPluginManager()
              .callEvent(new TrackableProgressUpdateEvent(
                  mutableStation.setMeta(m -> m.setInventory(result.getInventory())),player));
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
    TrackableProgressMeta meta = mutableStation.getMeta();
    StationInventory inventory = meta.getInventory();
    List<SmaugRecipe> recipes = Smaug.fetchAllRecipes(mutableStation, inventory.getContents());
    if (recipes.isEmpty()) {
      player.sendMessage("There are not any recipes available");
    }
    SmaugRecipe recipe = select(mutableStation,
        recipes, player);
    if (recipe == null || !recipe.test(inventory.getContents()).isSuccess()) {
      return;
    }

    final Location stationBlockLocation = mutableStation.blockLocation();
    if (recipe.getActions() > 0) {
      //bossBarManager.show(proxy);
      if (mutableStation.getMeta().getProgress() < recipe.getActions()) {
        successfulAction(stationBlockLocation);
        Bukkit.getPluginManager()
            .callEvent(new TrackableProgressUpdateEvent(
                mutableStation.setMeta(m -> m.setProgress(progress -> progress + 1)),player));
      } else {
        ItemStack stack = craftRecipeOutput(recipe);
        ItemAddResult result = inventory.setItems(recipe.getIngredients()
            .remove(inventory.getItems())).add(List.of(stack));
        if (result.isSuccess()) {
          player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
          Bukkit.getPluginManager()
              .callEvent(new TrackableProgressUpdateEvent(mutableStation.setMeta(m -> m.toBuilder()
                  .setRecipeKey(null)
                  .setProgress(0)
                  .setInventory(result.getInventory())),player));
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

  private SmaugRecipe select(MutableStation<TrackableProgressMeta> mutableStation, List<SmaugRecipe> recipes,
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
      mutableStation.setMeta(m -> m.toBuilder().setProgress(0).setRecipeKey(recipe.getKey()));
      Bukkit.getPluginManager().callEvent(new TrackableProgressUpdateEvent(mutableStation,player));
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
    private final IViewModel<MutableStation, BossBar> viewModel;

    BossBarManager(IViewModel<MutableStation, BossBar> viewModel) {
      this.viewModel = viewModel;
    }

    private void show(StationPlayerModelProxy proxy) {
      IViewModelBinding binding = viewModel.getBinding(proxy.mutableStation());
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
