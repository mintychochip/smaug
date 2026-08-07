package org.aincraft.container.refining;

import java.util.Objects;
import java.util.Optional;
import org.aincraft.container.ingredient.IngredientList;

public record InventoryPlanResult(
    Status status,
    Optional<InventoryPlan> plan,
    IngredientList missing) {

  public InventoryPlanResult {
    Objects.requireNonNull(status, "status");
    Objects.requireNonNull(plan, "plan");
    Objects.requireNonNull(missing, "missing");
    if (status == Status.SUCCESS && plan.isEmpty()) {
      throw new IllegalArgumentException("successful plan result requires a plan");
    }
    if (status != Status.SUCCESS && plan.isPresent()) {
      throw new IllegalArgumentException("failed plan result cannot contain a plan");
    }
  }

  public enum Status {
    SUCCESS,
    MISSING_INPUT,
    OUTPUT_FULL,
    INVALID_INPUT
  }
}
