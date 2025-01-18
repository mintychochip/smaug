package org.aincraft.inject.implementation.view;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.aincraft.api.event.RecipeProgressUpdateEvent;
import org.aincraft.container.IRecipeFetcher;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.database.model.RecipeProgress;
import org.aincraft.database.model.Station;
import org.aincraft.listener.IStationService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

@Singleton
public class ProgressBarControllerImpl implements
    IViewModelController<RecipeProgress, BossBar> {

  private static final Component DEFAULT_BOSS_BAR_ITEM_NAME = Component.text("default");
  private final Map<Key, IViewModel<RecipeProgress, BossBar>> viewModels = new HashMap<>();
  private final IStationService stationService;
  private final IRecipeFetcher recipeFetcher;
  private final Plugin plugin;

  public ProgressBarControllerImpl(IStationService stationService, IRecipeFetcher recipeFetcher,
      Plugin plugin) {
    this.stationService = stationService;
    this.recipeFetcher = recipeFetcher;
    this.plugin = plugin;
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

  @Override
  public void update(Object modelKey, Object... args) {
    Preconditions.checkArgument(modelKey instanceof UUID);
    Station station = stationService.getStation((UUID) modelKey);
    RecipeProgress progress = stationService.getRecipeProgress((UUID) modelKey);
    SmaugRecipe recipe = recipeFetcher.fetch(progress.getRecipeKey());
    if (recipe == null) {
      return;
    }
    IViewModel<RecipeProgress, BossBar> viewModel = viewModels.get(
        station.getStationKey());
    final ItemStack reference = recipe.getOutput().getReference();
    final ItemMeta itemMeta = reference.getItemMeta();
    @SuppressWarnings("UnstableApiUsage")
    final Component itemName = itemMeta.hasDisplayName() ? itemMeta.displayName()
        : reference.getDataOrDefault(DataComponentTypes.ITEM_NAME,
            DEFAULT_BOSS_BAR_ITEM_NAME);
    assert itemName != null;
    final Player player = (Player) args[0];
    viewModel.update(modelKey, progress.getProgress(), recipe.getActions(), itemName, player);
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
  private void handleUpdateBossBar(final RecipeProgressUpdateEvent event) {
    if (event.isCancelled()) {
      return;
    }
    final Station station = event.getStation();
    final RecipeProgress progress = event.getRecipeProgress();
    final Player player = event.getPlayer();
    CompletableFuture.supplyAsync(() -> stationService.updateRecipeProgress(progress))
        .thenAcceptAsync(b -> {
          new BukkitRunnable() {
            @Override
            public void run() {
              update(station.getId(), player);
            }
          }.runTask(plugin);
        });

  }

}
