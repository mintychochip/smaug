package org.aincraft.container.refining;

public record EfficiencyProfile(
    String key,
    int minimumLevel,
    double chancePerLevel,
    int maximumBonus) {

  public EfficiencyProfile {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("key is required");
    }
    if (minimumLevel < 0) {
      throw new IllegalArgumentException("minimumLevel is negative");
    }
    if (Double.isNaN(chancePerLevel) || chancePerLevel < 0 || chancePerLevel > 1) {
      throw new IllegalArgumentException("chancePerLevel must be between 0 and 1");
    }
    if (maximumBonus < 0) {
      throw new IllegalArgumentException("maximumBonus is negative");
    }
  }
}
