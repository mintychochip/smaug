package org.aincraft.inject.implementation.view;

import com.google.inject.Singleton;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.aincraft.api.event.RecipeProgressUpdateEvent;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.database.model.RecipeProgress;
import org.aincraft.database.model.Station;
import org.aincraft.inject.IRecipeFetcher;
import org.aincraft.listener.IStationService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

@Singleton
public class ProgressBarControllerImpl implements
    IViewModelController<RecipeProgress, BossBar> {

  private static final Component DEFAULT_BOSS_BAR_ITEM_NAME = Component.text("default");
  private final Map<Key, IViewModel<RecipeProgress, BossBar>> viewModels = new HashMap<>();
  private final IStationService stationService;
  private final IRecipeFetcher recipeFetcher;

  public ProgressBarControllerImpl(IStationService stationService, IRecipeFetcher recipeFetcher) {
    this.stationService = stationService;
    this.recipeFetcher = recipeFetcher;
  }

  @Override
  public void register(@NotNull Key stationKey,
      @NotNull IViewModel<RecipeProgress, BossBar> viewModel) {
    viewModels.put(stationKey, viewModel);
  }

  @Override
  public boolean isRegistered(@NotNull Key stationKey) {
    return viewModels.containsKey(stationKey);
  }

  @Override
  public IViewModel<RecipeProgress, BossBar> get(@NotNull Key stationKey) {
    return viewModels.get(stationKey);
  }

  public void update(RecipeProgress model, Player viewer) {
    Station station = stationService.getStation(model.getStationId());
    SmaugRecipe recipe = recipeFetcher.fetch(model.getRecipeKey());
    if (recipe == null) {
      return;
    }
    IViewModel<RecipeProgress, BossBar> viewModel = viewModels.get(
        station.getStationKey());
    final ItemStack reference = recipe.getOutput().getReference();
    final ItemMeta meta = reference.getItemMeta();
    @SuppressWarnings("UnstableApiUsage")
    final Component itemName = meta.hasDisplayName() ? meta.displayName()
        : reference.getDataOrDefault(DataComponentTypes.ITEM_NAME,
            DEFAULT_BOSS_BAR_ITEM_NAME);
    assert itemName != null;
    viewModel.update(model, model.getProgress(), recipe.getActions(), itemName, viewer);
  }

  @Override
  public Collection<IViewModel<RecipeProgress, BossBar>> getAll() {
    return viewModels.values();
  }

  @NotNull
  @Override
  public Iterator<IViewModel<RecipeProgress, BossBar>> iterator() {
    return viewModels.values().iterator();
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleUpdate(final RecipeProgressUpdateEvent event) {
    if(event.isCancelled()) {
      return;
    }
    final RecipeProgress model = event.getProgress();
    final Player player = event.getPlayer();
    this.update(model,player);
  }
}
