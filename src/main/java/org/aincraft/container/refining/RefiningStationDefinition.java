package org.aincraft.container.refining;

import java.util.Objects;

public record RefiningStationDefinition(
    RefiningStationType type,
    String displayName,
    String capabilityLabel) {

  public RefiningStationDefinition {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(displayName, "displayName");
    Objects.requireNonNull(capabilityLabel, "capabilityLabel");
    if (displayName.isBlank()) {
      throw new IllegalArgumentException("displayName is required");
    }
    if (capabilityLabel.isBlank()) {
      throw new IllegalArgumentException("capabilityLabel is required");
    }
  }

  public static RefiningStationDefinition of(RefiningStationType type) {
    Objects.requireNonNull(type, "type");
    return new RefiningStationDefinition(type, type.displayName(), type.professionKey());
  }
}
