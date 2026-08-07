package org.aincraft.container.refining;

import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

public record RefiningSession(
    UUID playerId,
    UUID stationId,
    Key stationKey,
    @Nullable String selectedRecipeKey,
    int batch,
    boolean executing) {

  public RefiningSession {
    Objects.requireNonNull(playerId, "playerId");
    Objects.requireNonNull(stationId, "stationId");
    Objects.requireNonNull(stationKey, "stationKey");
    if (selectedRecipeKey != null && selectedRecipeKey.isBlank()) {
      throw new IllegalArgumentException("selectedRecipeKey must not be blank");
    }
    if (batch < 1) {
      throw new IllegalArgumentException("batch must be positive");
    }
  }
}
