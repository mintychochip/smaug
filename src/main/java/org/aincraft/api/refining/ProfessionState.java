package org.aincraft.api.refining;

public record ProfessionState(boolean recipeKnown, int level) {

  public ProfessionState {
    if (level < 0) {
      throw new IllegalArgumentException("level must be non-negative");
    }
  }

  public static ProfessionState denied() {
    return new ProfessionState(false, 0);
  }
}
