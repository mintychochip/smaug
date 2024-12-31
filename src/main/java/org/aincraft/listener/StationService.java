package org.aincraft.listener;

import com.google.inject.Inject;
import java.util.concurrent.CompletableFuture;
import org.aincraft.api.event.StationAfterRemoveEvent;
import org.aincraft.api.event.StationBeforeRemoveEvent;
import org.aincraft.api.event.StationCreateEvent;
import org.aincraft.api.event.StationRemoveEvent.RemovalCause;
import org.aincraft.model.Station;
import org.aincraft.storage.IStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

public final class StationService {

  private final IStorage storage;
  private final Plugin plugin;

  @Inject
  public StationService(Plugin plugin, IStorage storage) {
    this.plugin = plugin;
    this.storage = storage;
  }

  public void createStation(String stationKey, Location location,
      @Nullable Player player) {
    CompletableFuture.supplyAsync(() -> storage.hasStation(location))
        .thenAcceptAsync(exists -> {
          if (exists) {
            return;
          }
          CompletableFuture.supplyAsync(() -> storage.createStation(stationKey, location))
              .thenAccept(station -> {
                if (station != null) {
                  new BukkitRunnable() {
                    @Override
                    public void run() {
                      Bukkit.getPluginManager()
                          .callEvent(new StationCreateEvent(station, player));
                    }
                  }.runTask(plugin);
                }
              });
        });
  }

  public void deleteStation(Location location, @Nullable Player player,
      RemovalCause removalCause) {
    CompletableFuture.supplyAsync(() -> storage.getStation(location)).thenAcceptAsync(station -> {
      if (station == null) {
        return;
      }
      new BukkitRunnable() {
        @Override
        public void run() {
          Bukkit.getPluginManager()
              .callEvent(new StationBeforeRemoveEvent(station, player, removalCause));
          CompletableFuture.runAsync(() -> {
            storage.deleteStation(location);
            new BukkitRunnable() {
              @Override
              public void run() {
                Bukkit.getPluginManager()
                    .callEvent(new StationAfterRemoveEvent(station, player, removalCause));
              }
            }.runTask(plugin);
          });
        }
      }.runTask(plugin);
    });
  }

}
