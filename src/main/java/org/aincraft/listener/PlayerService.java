/*
 * MIT License
 *
 * Copyright (c) 2025 mintychochip
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * provided to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
