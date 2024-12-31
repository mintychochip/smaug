package org.aincraft.listener;

import com.google.inject.Inject;
import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.aincraft.model.StationUser;
import org.aincraft.storage.IStorage;
import org.bukkit.entity.Player;

public final class PlayerService {

  private final IStorage storage;

  @Inject
  PlayerService(IStorage storage) {
    this.storage = storage;
  }

  public void updateUser(Player player) {
    String id = player.getUniqueId().toString();
    CompletableFuture.supplyAsync(() -> storage.getStationUser(id)).thenAcceptAsync(user -> {
      if (user == null) {
        return;
      }
      Timestamp userJoined = user.getJoined();
      storage.updateStationUser(new StationUser(id, player.getName(), userJoined));
    });
  }

  public void addUser(Player player) {
    String id = player.getUniqueId().toString();
    CompletableFuture.runAsync(() -> {
      storage.createStationUser(id, player.getName());
    });
  }

  public void userExists(Player player, Consumer<Boolean> callback) {
    String id = player.getUniqueId().toString();
    CompletableFuture.supplyAsync(() -> storage.hasStationUser(id)).thenAccept(callback)
        .exceptionally(ex -> {
          ex.printStackTrace();
          return null;
        });
  }
}
