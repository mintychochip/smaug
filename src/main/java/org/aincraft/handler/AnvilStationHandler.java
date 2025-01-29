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

package org.aincraft.handler;

import com.google.inject.name.Named;
import dev.triumphteam.gui.guis.Gui;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.aincraft.Smaug;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.StationHandler;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.gui.AnvilGuiProxy.RecipeSelectorItem;
import org.aincraft.container.ingredient.IngredientList;
import org.aincraft.container.item.ItemStackBuilder;
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
import org.aincraft.database.model.Station.StationMeta;
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

public class AnvilStationHandler implements StationHandler {

  private final IStationService service;
  private final NamespacedKey idKey;
  private final IViewModel<StationPlayerModelProxy, AnvilGuiProxy> guiViewModel;
  private final BossBarManager bossBarManager;

  public AnvilStationHandler(IStationService service,
      @Named("id") NamespacedKey idKey,
      IViewModel<StationPlayerModelProxy, AnvilGuiProxy> viewModel,
      IViewModel<Station, BossBar> bossBarViewModel) {
    this.service = service;
    this.idKey = idKey;
    this.guiViewModel = viewModel;
    this.bossBarManager = new BossBarManager(bossBarViewModel);
  }

  @Override
  public void handle(final Context ctx) {
    final Player player = ctx.getPlayer();
    final ItemStack item = ctx.getItem();
    final Station station = ctx.getStation();
    final StationPlayerModelProxy proxy = new StationPlayerModelProxy(player, station);
    if (ctx.isRightClick()) {
      ctx.cancel();
      if (item != null) {
        ItemAddResult result = station.getMeta().getInventory().add(List.of(item));
        if (result.isSuccess()) {
          Bukkit.getPluginManager()
              .callEvent(new StationUpdateEvent(
                  station.setMeta(m -> m.setInventory(result.getInventory())), player));
          player.sendMessage(Component.empty().color(
                  NamedTextColor.WHITE).append(Component.text("Deposited:"))
              .append(item.displayName()));
        }
      } else {
        openMenu(guiViewModel, proxy);
      }
      return;
    }
    if (player.isSneaking() || !ItemIdentifier.contains(item, idKey, "hammer")) {
      return;
    }
    ctx.cancel();
    StationMeta meta = station.getMeta();
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
      bossBarManager.show(proxy);
      if (station.getMeta().getProgress() < recipe.getActions()) {
        successfulAction(stationBlockLocation);
        Bukkit.getPluginManager()
            .callEvent(new StationUpdateEvent(
                station.setMeta(m -> m.setProgress(progress -> progress + 1)), player));
      } else {
        ItemStack stack = craftRecipeOutput(recipe);
        ItemAddResult result = inventory.setItems(recipe.getIngredients()
            .remove(inventory.getItems())).add(List.of(stack));
        if (result.isSuccess()) {
          player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
          Bukkit.getPluginManager()
              .callEvent(new StationUpdateEvent(station.setMeta(m -> m.setRecipeKey(null)
                  .setProgress(0)
                  .setInventory(result.getInventory())),
                  player));
        }
      }
    } else {
      final IngredientList ingredientList = recipe.getIngredients();
      final Map<Integer, ItemStack> removed = ingredientList.remove(inventory.getItems());
      final ItemAddResult result = inventory.setItems(removed).add(craftRecipeOutput(recipe));
      if (result.isSuccess()) {
        station.setMeta(
            m -> m.setRecipeKey(null).setProgress(0).setInventory(result.getInventory()));

      }
    }
  }

  @NotNull
  private static ItemStack craftRecipeOutput(SmaugRecipe recipe) {
    final IKeyedItem item = recipe.getOutput();
    final ItemStack reference = item.getReference();
    return ItemStackBuilder.create(reference).setAmount(recipe.getAmount()).build();
  }

  private static void openMenu(IViewModel<StationPlayerModelProxy, AnvilGuiProxy> viewModel,
      StationPlayerModelProxy proxy) {
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

  private SmaugRecipe select(Station station, List<SmaugRecipe> recipes, Player player) {
    final StationMeta meta = station.getMeta();
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
      IViewModelBinding binding = guiViewModel.getBinding(
          new StationPlayerModelProxy(player, station));
      RecipeSelectorItem selectorItem = binding.getProperty("recipe-selector",
          RecipeSelectorItem.class);
      selectorItem.recipeSelectorGui().open(player);
    }
    if (size == 1) {
      SmaugRecipe recipe = recipes.getFirst();
      station.setMeta(m -> m.setProgress(0).setRecipeKey(recipe.getKey()));
      Bukkit.getPluginManager().callEvent(new StationUpdateEvent(station, player));
      return recipe;
    }
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
