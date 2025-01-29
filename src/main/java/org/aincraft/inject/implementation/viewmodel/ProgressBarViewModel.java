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

package org.aincraft.inject.implementation.viewmodel;

import io.papermc.paper.datacomponent.DataComponentTypes;

import java.util.UUID;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.aincraft.container.IFactory;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.display.PropertyNotFoundException;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationMeta;
import org.aincraft.exception.ForwardReferenceException;
import org.aincraft.exception.UndefinedRecipeException;
import org.aincraft.inject.IRecipeFetcher;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

final class ProgressBarViewModel extends AbstractViewModel<Station, BossBar, UUID> {

  private static final Color DEFAULT_BOSS_BAR_COLOR = Color.BLUE;

  private final IRecipeFetcher recipeFetcher;

  ProgressBarViewModel(IRecipeFetcher recipeFetcher) {
    this.recipeFetcher = recipeFetcher;
  }

  static final class BossBarViewModelBinding extends AbstractBinding {

    @ExposedProperty("bossbar")
    private final BossBar bossBar;

    BossBarViewModelBinding(BossBar bossBar) {
      this.bossBar = bossBar;
    }

    public BossBar getBossBar() {
      return bossBar;
    }
  }

  @Override
  public void update(@NotNull Station model) {
    try {
      final BossBar reference = this.getBinding(model).getProperty("bossbar", BossBar.class);
      updateBossBar(reference, model);
    } catch (PropertyNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @NotNull Class<? extends IViewModelBinding> getBindingClass() {
    return BossBarViewModelBinding.class;
  }

  @Override
  @NotNull
  IFactory<BossBar, Station> getViewFactory() {
    return data -> {
      BossBar bossBar = BossBar.bossBar(Component.empty(), 0, DEFAULT_BOSS_BAR_COLOR,
          Overlay.PROGRESS);
      if (data == null) {
        return bossBar;
      }
      updateBossBar(bossBar, data);
      return bossBar;
    };
  }

  @Override
  @NotNull
  IViewModelBinding viewToBinding(@NotNull BossBar view) {
    return new BossBarViewModelBinding(view);
  }

  @Override
  @NotNull UUID modelToKey(@NotNull Station model) {
    return model.id();
  }

  private void updateBossBar(@NotNull BossBar reference, Station station) {
    final StationMeta meta = station.getMeta();
    try {
      final String recipeKey = meta.getRecipeKey();
      if(recipeKey == null) {
        return;
      }
      final SmaugRecipe recipe = recipeFetcher.fetch(recipeKey);
      final float actions = recipe.getActions();
      final float progress = meta.getProgress();
      final Component formattedBossBarName = MiniMessage.miniMessage()
          .deserialize("Forging: <a> (<b>)",
              Placeholder.component("a", retrieveItemName(recipe)),
              Placeholder.component("b", Component.text(actions - progress)));
      reference.progress(progress / actions).name(formattedBossBarName);
    } catch (ForwardReferenceException | UndefinedRecipeException e) {
      throw new RuntimeException(e);
    }
  }

  @NotNull
  private static Component retrieveItemName(@NotNull SmaugRecipe recipe) {
    final ItemStack reference = recipe.getOutput().getReference();
    final ItemMeta itemMeta = reference.getItemMeta();
    @SuppressWarnings("UnstableApiUsage") final Component itemName =
        itemMeta.hasDisplayName() ? itemMeta.displayName()
            : reference.getDataOrDefault(DataComponentTypes.ITEM_NAME,
                Component.empty());
    assert itemName != null;
    return itemName;
  }

}
