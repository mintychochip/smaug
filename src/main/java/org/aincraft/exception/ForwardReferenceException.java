/*
 * MIT License
 *
 * Copyright (c) 2025 mintychochip
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * provided to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.aincraft.exception;

/**
 * <p>
 *   Thrown when an (item) ingredient in a recipe cannot be resolved, either because the
 *   item is not registered in the registry, or the key associated with the item cannot be resolved.
 * </p>
 * <p>
 *   This can occur during recipe parsing when the item key used in the recipe refers to an item
 *   that does not exist in the registry.
 * </p>
 * <p>
 *   Before throwing this exception, you can check whether an item key is valid using:
 *   {@code IItemRegistry::check(string, true)} or by checking if
 *   {@code IItemRegistry::resolve(string, true)} returns non-null.
 * </p>
 */
public class ForwardReferenceException extends RuntimeException {

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
