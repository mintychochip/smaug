package org.aincraft.inject.implementation.view;

import com.google.inject.Singleton;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.SmaugRecipe;
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.display.IViewModelController;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationMeta;
import org.aincraft.inject.IRecipeFetcher;
import org.aincraft.listener.IStationService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

@Singleton
public class ProgressBarControllerImpl implements
    IViewModelController<Station, BossBar> {

  private static final Component DEFAULT_BOSS_BAR_ITEM_NAME = Component.text("default");
  private final Map<Key, IViewModel<Station, BossBar>> viewModels = new HashMap<>();
  private final IRecipeFetcher recipeFetcher;

  public ProgressBarControllerImpl(IRecipeFetcher recipeFetcher) {
    this.recipeFetcher = recipeFetcher;
  }

  @Override
  public void register(@NotNull Key stationKey,
      @NotNull IViewModel<Station, BossBar> viewModel) {
    viewModels.put(stationKey, viewModel);
  }

  @Override
  public boolean isRegistered(@NotNull Key stationKey) {
    return viewModels.containsKey(stationKey);
  }

  @Override
  public IViewModel<Station, BossBar> get(@NotNull Key stationKey) {
    return viewModels.get(stationKey);
  }

  public void update(Station model, Player viewer) {
    StationMeta meta = model.getMeta();
    String recipeKey = meta.getRecipeKey();
    if(recipeKey == null) {
      return;
    }
    SmaugRecipe recipe = recipeFetcher.fetch(recipeKey);
    if (recipe == null) {
      return;
    }
    IViewModel<Station, BossBar> viewModel = viewModels.get(
        model.stationKey());
    final ItemStack reference = recipe.getOutput().getReference();
    final ItemMeta itemMeta = reference.getItemMeta();
    @SuppressWarnings("UnstableApiUsage") final Component itemName =
        itemMeta.hasDisplayName() ? itemMeta.displayName()
            : reference.getDataOrDefault(DataComponentTypes.ITEM_NAME,
                DEFAULT_BOSS_BAR_ITEM_NAME);
    assert itemName != null;
    viewModel.update(model, meta.getProgress(), recipe.getActions(), itemName, viewer);
  }

  @Override
  public Collection<IViewModel<Station, BossBar>> getAll() {
    return viewModels.values();
  }

  @NotNull
  @Override
  public Iterator<IViewModel<Station, BossBar>> iterator() {
    return viewModels.values().iterator();
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleUpdate(final StationUpdateEvent event) {
    if (event.isCancelled()) {
      return;
    }
    final Station model = event.getModel();
    final Player player = event.getViewer();
    this.update(model, player);
  }
}
