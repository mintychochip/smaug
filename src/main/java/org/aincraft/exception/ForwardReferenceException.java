package org.aincraft.exception;

/**
 * This exception is thrown when parsing a recipe, if the recipe ingredients cannot be resolved
 */
public class ForwardReferenceException extends Exception {

  private final String offendingKey;

  public ForwardReferenceException(String offendingKey) {
    this.offendingKey = offendingKey;
  }

  public String getOffendingKey() {
    return offendingKey;
  }
}
