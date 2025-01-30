///*
// *
// * Copyright (C) 2025 mintychochip
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// *
// */
//
//package org.aincraft.inject.implementation.viewmodel;
//
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//import net.kyori.adventure.key.Key;
//import org.aincraft.container.display.IViewModel;
//import org.aincraft.container.display.IViewModelController;
//import org.jetbrains.annotations.NotNull;
//
//public abstract class AbstractViewModelController<M, V> implements IViewModelController<M, V> {
//
//  protected final Map<Key, IViewModel<M, V>> viewModels = new HashMap<>();
//
//  @Override
//  public void register(@NotNull Key stationKey, @NotNull IViewModel<M, V> viewModel) {
//    viewModels.put(stationKey, viewModel);
//  }
//
//  @Override
//  public boolean isRegistered(@NotNull Key stationKey) {
//    return viewModels.containsKey(stationKey);
//  }
//
//  @Override
//  public IViewModel<M, V> get(@NotNull Key stationKey) {
//    return viewModels.get(stationKey);
//  }
//
//  @Override
//  public Collection<IViewModel<M, V>> getAll() {
//    return viewModels.values();
//  }
//
//  @NotNull
//  @Override
//  public Iterator<IViewModel<M, V>> iterator() {
//    return viewModels.values().iterator();
//  }
//}
