package org.aincraft.api.refining;

public record RefiningStationAccessResult(boolean allowed, int stationTier) {

  public RefiningStationAccessResult {
    if (stationTier < 0) {
      throw new IllegalArgumentException("stationTier must be non-negative");
    }
  }

  public static RefiningStationAccessResult denied() {
    return new RefiningStationAccessResult(false, 0);
  }
}
