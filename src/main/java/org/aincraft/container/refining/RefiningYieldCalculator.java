package org.aincraft.container.refining;

import java.util.Objects;

@FunctionalInterface
public interface RefiningYieldCalculator {

  RefiningYieldResult calculate(
      RefiningMetadata metadata,
      int professionLevel,
      int baseAmount,
      double randomUnit);

  static RefiningYieldCalculator standard() {
    return (metadata, professionLevel, baseAmount, randomUnit) -> {
      Objects.requireNonNull(metadata, "metadata");
      if (professionLevel < 0 || baseAmount < 0) {
        throw new IllegalArgumentException("profession level and base amount must be non-negative");
      }

      EfficiencyProfile profile = metadata.efficiencyProfile();
      if (professionLevel < profile.minimumLevel() || profile.maximumBonus() == 0
          || profile.chancePerLevel() == 0.0) {
        return new RefiningYieldResult(baseAmount, 0);
      }

      long eligibleLevels = (long) professionLevel - profile.minimumLevel() + 1L;
      double chance = Math.min(1.0, eligibleLevels * profile.chancePerLevel());
      double normalizedRandom = normalize(randomUnit);
      int bonus = normalizedRandom < chance ? profile.maximumBonus() : 0;
      return new RefiningYieldResult(baseAmount + bonus, bonus);
    };
  }

  private static double normalize(double randomUnit) {
    if (Double.isNaN(randomUnit) || randomUnit <= 0.0) {
      return 0.0;
    }
    return Math.min(randomUnit, Math.nextDown(1.0));
  }
}
