package org.aincraft.exception;

/**
 * <p>
 *   Thrown when an (item) ingredient in a recipe cannot be resolved, either because the
 *   item is not registered in the registry, or the key associated with the item cannot be resolved.
 * </p>
 * <p>
 *   This can occur during recipe parsing when the item key used in the recipe refers to an item
 *   that does not exist in the registry or has an invalid reference.
 * </p>
 * <p>
 *   Before throwing this exception, you can check whether an item key is valid using:
 *   {@code IItemRegistry::check(string, true)} or by checking if
 *   {@code IItemRegistry::resolve(string, true)} returns non-null.
 * </p>
 */
public class ForwardReferenceException extends Exception {

  /**
   * The offending item key
   */
  private final String itemKey;

  public ForwardReferenceException(String offendingKey) {
    super("failed to locate forward reference: %s, consider if the item was registered properly".formatted(offendingKey));
    this.itemKey = offendingKey;
  }

  public String getItemKey() {
    return itemKey;
  }
}
