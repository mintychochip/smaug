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

package org.aincraft.inject.implementation.viewmodel;

import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.aincraft.Smaug;
import org.aincraft.container.IFactory;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.display.PropertyNotFoundException;
import org.aincraft.database.model.meta.TrackableProgressMeta;
import org.aincraft.database.model.test.IMetaStation;
import org.aincraft.exception.ForwardReferenceException;
import org.aincraft.exception.UndefinedRecipeException;
import org.aincraft.inject.IRecipeFetcher;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public final class ProgressBarViewModel extends
    AbstractViewModel<IMetaStation<TrackableProgressMeta>, BossBar, UUID> {

  private static final Color DEFAULT_BOSS_BAR_COLOR = Color.BLUE;

  @Override
  public void update(@NotNull IMetaStation<TrackableProgressMeta> model) {
    try {
      final BossBar reference = this.getBinding(model).getProperty("bossbar", BossBar.class);
      updateBossBar(reference, model);
    } catch (PropertyNotFoundException e) {
      throw new RuntimeException(e);
    }
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
  @NotNull
  Class<? extends IViewModelBinding> getBindingClass() {
    return BossBarViewModelBinding.class;
  }

  @Override
  @NotNull
  IFactory<BossBar, IMetaStation<TrackableProgressMeta>> getViewFactory() {
    return data -> {
      BossBar bossBar = BossBar.bossBar(Component.empty(), 0, DEFAULT_BOSS_BAR_COLOR,
          Overlay.PROGRESS);
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
  @NotNull UUID modelToKey(@NotNull IMetaStation<TrackableProgressMeta> model) {
    return model.getId();
  }

  private void updateBossBar(@NotNull BossBar reference, IMetaStation<TrackableProgressMeta> station) {
    final TrackableProgressMeta meta = station.getMeta();
    try {
      final String recipeKey = meta.getRecipeKey();
      if (recipeKey == null) {
        return;
      }
      final SmaugRecipe recipe = Smaug.fetchRecipe(recipeKey);
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
