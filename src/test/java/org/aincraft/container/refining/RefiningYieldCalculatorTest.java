package org.aincraft.container.refining;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RefiningYieldCalculatorTest {

  private final RefiningYieldCalculator calculator = RefiningYieldCalculator.standard();

  @Test
  void belowMinimumReturnsBaseOutput() {
    RefiningYieldResult result = calculator.calculate(metadata(10, 1.0, 4), 9, 2, 0.0);

    assertEquals(2, result.finalAmount());
    assertEquals(0, result.bonusAmount());
  }

  @Test
  void exactThresholdUsesTheProfileChance() {
    RefiningMetadata metadata = metadata(10, 0.5, 3);

    RefiningYieldResult successfulRoll = calculator.calculate(metadata, 10, 1, 0.49);
    RefiningYieldResult failedRoll = calculator.calculate(metadata, 10, 1, 0.5);

    assertEquals(4, successfulRoll.finalAmount());
    assertEquals(3, successfulRoll.bonusAmount());
    assertEquals(1, failedRoll.finalAmount());
    assertEquals(0, failedRoll.bonusAmount());
  }

  @Test
  void zeroProbabilityProducesNoBonus() {
    RefiningYieldResult result = calculator.calculate(metadata(0, 0.0, 5), 50, 4, 0.0);

    assertEquals(4, result.finalAmount());
    assertEquals(0, result.bonusAmount());
  }

  @Test
  void probabilityOneProducesTheMaximumBonus() {
    RefiningYieldResult result = calculator.calculate(metadata(0, 1.0, 5), 50, 4, 0.999999);

    assertEquals(9, result.finalAmount());
    assertEquals(5, result.bonusAmount());
  }

  @Test
  void bonusNeverExceedsProfileMaximum() {
    RefiningYieldResult result = calculator.calculate(metadata(0, 1.0, 2), 100, 7, 0.0);

    assertEquals(9, result.finalAmount());
    assertEquals(2, result.bonusAmount());
  }

  @Test
  void randomUnitIsClampedToValidRange() {
    RefiningMetadata metadata = metadata(0, 0.5, 1);

    RefiningYieldResult negativeRoll = calculator.calculate(metadata, 0, 1, -1.0);
    RefiningYieldResult oversizedRoll = calculator.calculate(metadata, 0, 1, 1.5);
    RefiningYieldResult nanRoll = calculator.calculate(metadata, 0, 1, Double.NaN);

    assertEquals(2, negativeRoll.finalAmount());
    assertEquals(1, negativeRoll.bonusAmount());
    assertEquals(1, oversizedRoll.finalAmount());
    assertEquals(0, oversizedRoll.bonusAmount());
    assertEquals(2, nanRoll.finalAmount());
    assertEquals(1, nanRoll.bonusAmount());
  }

  private static RefiningMetadata metadata(int minimumLevel, double chancePerLevel,
      int maximumBonus) {
    return new RefiningMetadata("smelting", 0, 1, 1,
        new EfficiencyProfile("smelting_default", minimumLevel, chancePerLevel, maximumBonus));
  }
}
