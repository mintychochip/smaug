/*
 *
 * Copyright (C) 2025 mintychochip
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
