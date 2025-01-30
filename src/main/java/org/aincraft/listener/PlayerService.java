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

package org.aincraft.listener;

import com.google.inject.Inject;
import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.aincraft.database.model.StationUser;
import org.aincraft.database.storage.IStorage;
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
