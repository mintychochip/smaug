package org.aincraft.container.refining;

import java.util.Objects;

public record RefiningMetadata(
    String professionKey,
    int requiredProfessionLevel,
    int requiredStationTier,
    int professionXp,
    EfficiencyProfile efficiencyProfile) {

  public RefiningMetadata {
    if (professionKey == null || professionKey.isBlank()) {
      throw new IllegalArgumentException("professionKey is required");
    }
    if (requiredProfessionLevel < 0 || requiredStationTier < 1 || professionXp < 0) {
      throw new IllegalArgumentException("invalid refining requirement");
    }
    Objects.requireNonNull(efficiencyProfile, "efficiencyProfile");
  }
}
