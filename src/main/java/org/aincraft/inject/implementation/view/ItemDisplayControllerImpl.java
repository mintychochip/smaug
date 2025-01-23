package org.aincraft.inject.implementation.view;

import com.google.inject.Singleton;
import java.util.List;
import java.util.function.Consumer;
import org.aincraft.api.event.StationRemoveEvent;
import org.aincraft.api.event.StationUpdateEvent;
import org.aincraft.container.display.AnvilItemDisplayView;
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.display.IViewModel.IViewModelBinding;
import org.aincraft.container.display.PropertyNotFoundException;
import org.aincraft.database.model.Station;
import org.aincraft.inject.implementation.controller.AbstractViewModelController;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@Singleton
final class ItemDisplayControllerImpl extends
    AbstractViewModelController<Station, AnvilItemDisplayView> {

  private static final Consumer<IViewModelBinding> REMOVE_ENTITY_CONSUMER = binding -> {
    try {
      @SuppressWarnings("unchecked")
      List<Display> displays = (List<Display>) binding.getProperty(
          "displays", List.class);
      displays.forEach(Entity::remove);
    } catch (PropertyNotFoundException e) {
      throw new RuntimeException(e);
    }
  };


  @EventHandler(priority = EventPriority.MONITOR)
  private void handleUpdateItemDisplay(final StationUpdateEvent event) {
    if (event.isCancelled()) {
      return;
    }
    final Station model = event.getModel();
    final IViewModel<Station, AnvilItemDisplayView> viewModel = this.get(model.stationKey());
    if(viewModel == null) {
      return;
    }
    viewModel.update(model);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void handleRemoveItemDisplay(final StationRemoveEvent event) {
    if (event.isCancelled()) {
      return;
    }
    Station station = event.getStation();
    IViewModel<Station, AnvilItemDisplayView> viewModel = this.get(station.stationKey());
    if (viewModel == null) {
      return;
    }
    viewModel.remove(station, REMOVE_ENTITY_CONSUMER);
  }
}
