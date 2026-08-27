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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.aincraft.Smaug;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.display.ViewModel;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationMeta;
import org.aincraft.exception.ForwardReferenceException;
import org.aincraft.exception.UndefinedRecipeException;
import org.aincraft.inject.IRecipeFetcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * Station progress boss bar projection.
 * <ul>
 *   <li>State → look: {@link #update} / event fan via controller</li>
 *   <li>Intent → show: {@link #showTemporarily} after a successful hammer action</li>
 * </ul>
 */
public final class ProgressBarViewModel extends ViewModel<Station, ProgressBarViewModel.BossBarBinding> {

  private static final Color DEFAULT_BOSS_BAR_COLOR = Color.BLUE;
  private static final long SHOW_TICKS = 20L;

  private final IRecipeFetcher recipeFetcher;
  /** player UUID + station UUID → hide task id */
  private final Map<ShowKey, Integer> hideTasks = new HashMap<>();

  public ProgressBarViewModel(IRecipeFetcher recipeFetcher) {
    this.recipeFetcher = recipeFetcher;
  }

  /** Direct typed binding for a station's progress boss bar. */
  public record BossBarBinding(BossBar bossBar) {}

  private record ShowKey(UUID playerId, UUID stationId) {}

  @Override
  protected @NotNull Object keyOf(@NotNull Station model) {
    return model.id();
  }

  @Override
  protected @NotNull BossBarBinding createBinding(@NotNull Station model) {
    BossBar bossBar = BossBar.bossBar(Component.empty(), 0, DEFAULT_BOSS_BAR_COLOR,
        Overlay.PROGRESS);
    updateBossBar(bossBar, model);
    return new BossBarBinding(bossBar);
  }

  @Override
  public void update(@NotNull Station model) {
    final BossBarBinding binding = this.findBinding(model);
    if (binding == null) {
      return;
    }
    updateBossBar(binding.bossBar(), model);
  }

  /**
   * Intentional show path: ensure binding, project state, show bar briefly to the player.
   */
  public void showTemporarily(@NotNull Player player, @NotNull Station station) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(station, "station");
    BossBarBinding binding = getBinding(station);
    updateBossBar(binding.bossBar(), station);
    BossBar bossBar = binding.bossBar();
    if (bossBar == null) {
      return;
    }
    if (!playerIsViewingBossBar(player, bossBar)) {
      player.showBossBar(bossBar);
    }
    ShowKey key = new ShowKey(player.getUniqueId(), station.id());
    Integer previousTaskId = hideTasks.remove(key);
    if (previousTaskId != null) {
      Bukkit.getScheduler().cancelTask(previousTaskId);
    }
    Plugin plugin = Smaug.getPlugin();
    int taskId = new BukkitRunnable() {
      @Override
      public void run() {
        hideTasks.remove(key);
        player.hideBossBar(bossBar);
      }
    }.runTaskLater(plugin, SHOW_TICKS).getTaskId();
    hideTasks.put(key, taskId);
  }

  @Override
  public void removeAll() {
    for (Integer taskId : hideTasks.values()) {
      if (taskId != null) {
        Bukkit.getScheduler().cancelTask(taskId);
      }
    }
    hideTasks.clear();
    super.removeAll();
  }

  /**
   * Cancel hide tasks for one station; bar itself is not player-owned after remove.
   */
  public void removeStation(@NotNull Station station) {
    UUID stationId = station.id();
    hideTasks.entrySet().removeIf(e -> {
      if (!e.getKey().stationId().equals(stationId)) {
        return false;
      }
      Integer taskId = e.getValue();
      if (taskId != null) {
        Bukkit.getScheduler().cancelTask(taskId);
      }
      return true;
    });
    remove(station);
  }

  private void updateBossBar(@NotNull BossBar reference, Station station) {
    final StationMeta meta = station.getMeta();
    try {
      final String recipeKey = meta.getRecipeKey();
      if (recipeKey == null) {
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

  private static boolean playerIsViewingBossBar(Player player, BossBar bossBar) {
    for (BossBar activeBossBar : player.activeBossBars()) {
      if (activeBossBar.equals(bossBar)) {
        return true;
      }
    }
    return false;
  }
}
