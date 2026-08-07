package org.aincraft.container.refining;

public record InventoryTransactionResult(Status status) {

  public enum Status {
    SUCCESS,
    STALE_INVENTORY,
    OUTPUT_INSERTION_FAILED
  }
}
