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

package org.aincraft;

import org.aincraft.container.IRegistry.IItemRegistry;
import org.aincraft.handler.StationHandler;
import org.aincraft.inject.IKeyFactory;
import org.aincraft.inject.IRecipeFetcher;
import org.aincraft.listener.IStationService;
import org.bukkit.plugin.Plugin;

public interface ISmaugPlugin {
  IRecipeFetcher getRecipeFetcher();
  IKeyFactory getKeyFactory();
  Plugin getPlugin();
  IStationService getStationService();
  IItemRegistry getItemRegistry();
  void registerHandler(StationHandler handler);
}
