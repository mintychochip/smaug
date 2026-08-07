package org.aincraft.container.refining;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.aincraft.database.model.Station;
import org.bukkit.entity.Player;

public final class RefiningSessionStore {

  private final Map<SessionKey, RefiningSession> sessions = new HashMap<>();

  public synchronized RefiningSession open(Player player, Station station) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(station, "station");
    SessionKey key = key(player, station);
    return sessions.computeIfAbsent(key,
        ignored -> new RefiningSession(player.getUniqueId(), station.id(), station.stationKey(),
            null, 1, false));
  }

  public synchronized Optional<RefiningSession> get(Player player, Station station) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(station, "station");
    return Optional.ofNullable(sessions.get(key(player, station)));
  }

  public synchronized void select(Player player, Station station, String recipeKey, int batch) {
    Objects.requireNonNull(recipeKey, "recipeKey");
    RefiningSession current = open(player, station);
    sessions.put(key(player, station), new RefiningSession(current.playerId(), current.stationId(),
        current.stationKey(), recipeKey, batch, current.executing()));
  }

  public synchronized void setBatch(Player player, Station station, int batch) {
    if (batch < 1) {
      throw new IllegalArgumentException("batch must be positive");
    }
    RefiningSession current = open(player, station);
    sessions.put(key(player, station), new RefiningSession(current.playerId(), current.stationId(),
        current.stationKey(), current.selectedRecipeKey(), batch, current.executing()));
  }

  public synchronized boolean beginExecution(Player player, Station station) {
    RefiningSession current = open(player, station);
    if (current.executing()) {
      return false;
    }
    sessions.put(key(player, station), new RefiningSession(current.playerId(), current.stationId(),
        current.stationKey(), current.selectedRecipeKey(), current.batch(), true));
    return true;
  }

  public synchronized void endExecution(Player player, Station station) {
    RefiningSession current = sessions.get(key(player, station));
    if (current == null || !current.executing()) {
      return;
    }
    sessions.put(key(player, station), new RefiningSession(current.playerId(), current.stationId(),
        current.stationKey(), current.selectedRecipeKey(), current.batch(), false));
  }

  public synchronized void close(Player player, Station station) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(station, "station");
    sessions.remove(key(player, station));
  }

  public synchronized void closeAll(Player player) {
    Objects.requireNonNull(player, "player");
    UUID playerId = player.getUniqueId();
    sessions.keySet().removeIf(key -> key.playerId().equals(playerId));
  }

  private static SessionKey key(Player player, Station station) {
    return new SessionKey(player.getUniqueId(), station.id());
  }

  private record SessionKey(UUID playerId, UUID stationId) {
  }
}
