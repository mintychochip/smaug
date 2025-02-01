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

package org.aincraft.commands;

import com.google.inject.Inject;
import org.aincraft.database.model.MutableStation;
import org.aincraft.database.model.meta.TrackableProgressMeta;
import org.aincraft.listener.IMutableStationService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SmithCommand implements CommandExecutor {

  private final IMutableStationService<TrackableProgressMeta> service;

  @Inject
  public SmithCommand(IMutableStationService<TrackableProgressMeta> service) {
    this.service = service;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
      @NotNull String s, @NotNull String[] strings) {
    MutableStation<TrackableProgressMeta> mutableStation = service.getStation(
        ((Player) commandSender).getLocation());
    if(mutableStation != null) {
      Bukkit.broadcastMessage(mutableStation.toString());
    }
    return true;
  }
}

