package org.aincraft.container.refining;

import java.util.Objects;

public record RefiningResult(Status status, int finalOutputAmount, int awardedXp) {

  public RefiningResult {
    Objects.requireNonNull(status, "status");
    if (finalOutputAmount < 0 || awardedXp < 0) {
      throw new IllegalArgumentException("result amounts must be non-negative");
    }
  }

  public static RefiningResult failure(Status status) {
    if (status == Status.SUCCESS) {
      throw new IllegalArgumentException("success requires output data");
    }
    return new RefiningResult(status, 0, 0);
  }

  public enum Status {
    SUCCESS,
    DENIED_STATION,
    INVALID_TIER,
    UNKNOWN_RECIPE,
    LEVEL_TOO_LOW,
    MISSING_INPUT,
    OUTPUT_FULL,
    STALE_INVENTORY,
    ALREADY_EXECUTING,
    INVALID_BATCH
  }
}
