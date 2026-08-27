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

package org.aincraft.container.display;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Model → binding registry for station presentation projections.
 * <p>
 * This is not classic MVVM. It owns live view handles ({@code B}) keyed by a model
 * identity and knows how to project domain state into those handles via {@link #update}.
 * <p>
 * <b>Ownership lifecycle</b>
 * <ul>
 *   <li><b>Create</b> — intentional only: {@link #getBinding} (get-or-create) or
 *       {@link #bind}. Call from open/show paths, not from every domain event.</li>
 *   <li><b>Update</b> — {@link #update} projects current model state into an existing
 *       binding. Prefer {@link #updateIfBound} from event fans so idle models do not
 *       allocate view resources.</li>
 *   <li><b>Remove</b> — {@link #remove} / {@link #removeAll}; subclasses may free
 *       entities or other external resources via {@link #onRemove}.</li>
 * </ul>
 *
 * @param <M> model type
 * @param <B> binding type (plain data held per model — typed handles, not a property bag)
 */
public abstract class ViewModel<M, B> {

  private final Map<Object, B> bindings = new HashMap<>();

  protected abstract @NotNull Object keyOf(@NotNull M model);

  protected abstract @NotNull B createBinding(@NotNull M model);

  /**
   * Project {@code model} into its binding. Callers that must not allocate should use
   * {@link #updateIfBound} instead.
   */
  public abstract void update(@NotNull M model);

  /**
   * Free external resources held by a binding (entities, etc.). Default no-op.
   */
  protected void onRemove(@NotNull B binding) {
  }

  public @NotNull B bind(@NotNull M model, @NotNull B binding) {
    Preconditions.checkNotNull(model);
    Preconditions.checkNotNull(binding);
    bindings.put(keyOf(model), binding);
    return binding;
  }

  /**
   * Get existing binding or create one. Use for intentional open/show paths only.
   */
  public @NotNull B getBinding(@NotNull M model) {
    Preconditions.checkNotNull(model);
    Object key = keyOf(model);
    B existing = bindings.get(key);
    if (existing != null) {
      return existing;
    }
    return bind(model, createBinding(model));
  }

  /**
   * Lookup without creating. Returns {@code null} if never bound.
   */
  public @Nullable B findBinding(@NotNull M model) {
    Preconditions.checkNotNull(model);
    return bindings.get(keyOf(model));
  }

  public boolean isBound(@NotNull M model) {
    Preconditions.checkNotNull(model);
    return bindings.containsKey(keyOf(model));
  }

  /**
   * {@link #update} only if a binding already exists. Returns whether update ran.
   */
  public boolean updateIfBound(@NotNull M model) {
    Preconditions.checkNotNull(model);
    if (!isBound(model)) {
      return false;
    }
    update(model);
    return true;
  }

  public void remove(@NotNull M model) {
    remove(model, null);
  }

  public void remove(@NotNull M model, @Nullable Consumer<B> onRemove) {
    Preconditions.checkNotNull(model);
    B removed = bindings.remove(keyOf(model));
    if (removed != null) {
      this.onRemove(removed);
      if (onRemove != null) {
        onRemove.accept(removed);
      }
    }
  }

  public void removeAll() {
    removeAll(null);
  }

  public void removeAll(@Nullable Consumer<B> onRemove) {
    for (B binding : bindings.values()) {
      this.onRemove(binding);
      if (onRemove != null) {
        onRemove.accept(binding);
      }
    }
    bindings.clear();
  }

  protected void putBinding(@NotNull M model, @NotNull B binding) {
    bindings.put(keyOf(model), binding);
  }
}
