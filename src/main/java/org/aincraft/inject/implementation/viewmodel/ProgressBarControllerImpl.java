package org.aincraft.inject.implementation.viewmodel;

import com.google.inject.Singleton;
import net.kyori.adventure.bossbar.BossBar;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.display.IViewModel;
import org.aincraft.database.model.Station;
import org.aincraft.database.model.Station.StationMeta;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@Singleton
final class ProgressBarControllerImpl extends AbstractViewModelController<Station, BossBar> {

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleUpdate(final StationUpdateEvent event) {
    if (event.isCancelled()) {
      return;
    }
    final Station model = event.getModel();
    StationMeta meta = model.getMeta();
    String recipeKey = meta.getRecipeKey();
    if (recipeKey == null) {
      return;
    }
    IViewModel<Station, BossBar> viewModel = this.get(
        model.stationKey());
    if (viewModel == null) {
      return;
    }
    viewModel.update(model);
  }
}
