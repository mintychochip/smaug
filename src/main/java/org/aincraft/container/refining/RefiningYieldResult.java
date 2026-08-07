package org.aincraft.container.refining;

public record RefiningYieldResult(int finalAmount, int bonusAmount) {

  public RefiningYieldResult {
    if (finalAmount < 0 || bonusAmount < 0 || bonusAmount > finalAmount) {
      throw new IllegalArgumentException("invalid refining yield");
    }
  }
}
