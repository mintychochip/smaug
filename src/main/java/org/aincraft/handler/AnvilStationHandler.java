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

import java.util.List;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.aincraft.Smaug;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.anvil.StationPlayerModelProxy;
import org.aincraft.container.item.ItemStackBuilder;
import org.aincraft.exception.ForwardReferenceException;
import org.aincraft.exception.UndefinedRecipeException;
import org.aincraft.container.item.IKeyedItem;
import org.aincraft.container.item.ItemIdentifier;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationInventory;
import org.aincraft.database.model.Station.StationInventory.ItemAddResult;
import org.aincraft.database.model.Station.StationMeta;
import org.aincraft.inject.implementation.viewmodel.AnvilGuiViewModel;
import org.aincraft.inject.implementation.viewmodel.ProgressBarViewModel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Anvil interaction / domain commands. Presentation intent (open GUI, flash progress bar)
 * is delegated to the projections; this class does not dig into binding records.
 */
public final class AnvilStationHandler implements StationHandler {

  private final Key key;
  private final NamespacedKey idKey;
  private final AnvilGuiViewModel guiViewModel;
  private final ProgressBarViewModel progressBarViewModel;

  public AnvilStationHandler(Key key, NamespacedKey idKey,
      AnvilGuiViewModel guiViewModel,
      ProgressBarViewModel progressBarViewModel) {
    this.key = key;
    this.idKey = idKey;
    this.guiViewModel = guiViewModel;
    this.progressBarViewModel = progressBarViewModel;
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
        guiViewModel.openMain(proxy);
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
    SmaugRecipe recipe = select(station, recipes, player);
    if (recipe == null || !recipe.test(inventory.getContents()).isSuccess()) {
      return;
    }

    final Location stationBlockLocation = station.blockLocation();
    if (recipe.getActions() > 0) {
      progressBarViewModel.showTemporarily(player, station);
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
    }
  }

  @NotNull
  private static ItemStack craftRecipeOutput(SmaugRecipe recipe) {
    final IKeyedItem item = recipe.getOutput();
    final ItemStack reference = item.getReference();
    return ItemStackBuilder.create(reference).setAmount(recipe.getAmount()).build();
  }

  private static void successfulAction(@NotNull Location stationLocation) {
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
      guiViewModel.openRecipeSelector(new StationPlayerModelProxy(player, station));
    }
    if (size == 1) {
      SmaugRecipe recipe = recipes.getFirst();
      station.setMeta(m -> m.setProgress(0).setRecipeKey(recipe.getKey()));
      Bukkit.getPluginManager().callEvent(new StationUpdateEvent(station, player));
      return recipe;
    }
    return null;
  }

  @Override
  public @NotNull Key key() {
    return key;
  }
}
