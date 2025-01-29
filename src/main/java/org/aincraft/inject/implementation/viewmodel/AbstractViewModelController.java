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

package org.aincraft.inject.implementation.viewmodel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.kyori.adventure.key.Key;
import org.aincraft.container.display.IViewModel;
import org.aincraft.container.display.IViewModelController;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractViewModelController<M, V> implements IViewModelController<M, V> {

  protected final Map<Key, IViewModel<M, V>> viewModels = new HashMap<>();

  @Override
  public void register(@NotNull Key stationKey, @NotNull IViewModel<M, V> viewModel) {
    viewModels.put(stationKey, viewModel);
  }

  @Override
  public boolean isRegistered(@NotNull Key stationKey) {
    return viewModels.containsKey(stationKey);
  }

  @Override
  public IViewModel<M, V> get(@NotNull Key stationKey) {
    return viewModels.get(stationKey);
  }

  @Override
  public Collection<IViewModel<M, V>> getAll() {
    return viewModels.values();
  }

  @NotNull
  @Override
  public Iterator<IViewModel<M, V>> iterator() {
    return viewModels.values().iterator();
  }
}
