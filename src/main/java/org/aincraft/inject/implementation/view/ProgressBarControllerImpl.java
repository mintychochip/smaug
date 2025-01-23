package org.aincraft.inject.implementation.view;

import com.google.inject.Singleton;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationMeta;
import org.aincraft.inject.IRecipeFetcher;
import org.aincraft.inject.implementation.controller.AbstractViewModelController;
import org.aincraft.listener.IStationService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

@Singleton
public class ProgressBarControllerImpl extends AbstractViewModelController<Station, BossBar> {

  private static final Component DEFAULT_BOSS_BAR_ITEM_NAME = Component.text("default");
  private final IRecipeFetcher recipeFetcher;

  public ProgressBarControllerImpl(IRecipeFetcher recipeFetcher) {
    this.recipeFetcher = recipeFetcher;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleUpdate(final StationUpdateEvent event) {
    if (event.isCancelled()) {
      return;
    }
    final Station model = event.getModel();
    final Player player = event.getViewer();
    StationMeta meta = model.getMeta();
    String recipeKey = meta.getRecipeKey();
    if (recipeKey == null) {
      return;
    }
    SmaugRecipe recipe = recipeFetcher.fetch(recipeKey);
    if (recipe == null) {
      return;
    }
    IViewModel<Station, BossBar> viewModel = this.get(
        model.stationKey());
    if (viewModel == null) {
      return;
    }
    final Component itemName = itemName(recipe);

    viewModel.update(model, meta.getProgress(), recipe.getActions(), itemName, player);
  }

  private static Component itemName(SmaugRecipe recipe) {
    final ItemStack reference = recipe.getOutput().getReference();
    final ItemMeta itemMeta = reference.getItemMeta();
    @SuppressWarnings("UnstableApiUsage") final Component itemName =
        itemMeta.hasDisplayName() ? itemMeta.displayName()
            : reference.getDataOrDefault(DataComponentTypes.ITEM_NAME,
                DEFAULT_BOSS_BAR_ITEM_NAME);
    return itemName;
  }

}
